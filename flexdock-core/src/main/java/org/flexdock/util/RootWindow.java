/* Copyright (c) 2004 Christopher M Butler

 Permission is hereby granted, free of charge, to any person obtaining a copy of
 this software and associated documentation files (the "Software"), to deal in the
 Software without restriction, including without limitation the rights to use,
 copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
 Software, and to permit persons to whom the Software is furnished to do so, subject
 to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. */
package org.flexdock.util;

import javax.swing.*;
import java.awt.*;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * This class provides an abstraction of root containers used in Swing. It
 * allows transparent use of methods common to {@code JFrame}, {@code JApplet},
 * {@code JWindow}, and {@code JDialog} without making an outward distinction
 * between the different container types. This is accomplished by wrapping the
 * root component.
 *
 * @author Chris Butler
 */
public class RootWindow {
	private static final Map<Component, RootWindow> MAP_BY_ROOT_CONTAINER = new WeakHashMap<>();

	private LayoutManager maxedLayout;

	private WeakReference<Component> root;

	private HashMap<Object, Object> clientProperties;

	private static Component getRoot(Component component) {
		if (component == null) {
			return null;
		}

		if (isValidRootContainer(component)) {
			return component;
		}

		Container parent = component.getParent();
		while (parent != null && !isValidRootContainer(parent)) {
			parent = parent.getParent();
		}

		return parent;
	}

	/**
	 * Traverses the container hierarchy to locate the root container and
	 * returns corresponding {@code RootSwingContainer}. If {@code component} is
	 * {@code null}, a {@code null} reference is returned.
	 *
	 * @param component the container whose root we wish to find
	 * @return the enclosing {@code RootSwingcontainer}
	 */
	public static RootWindow getRootContainer(Component component) {
		Component root = getRoot(component);
		if (!isValidRootContainer(root)) {
			return null;
		}

		RootWindow container = MAP_BY_ROOT_CONTAINER.computeIfAbsent(root, RootWindow::new);

		if (container.getRootContainer() != root) {
			container.setRootContainer(root);
		}

		return container;
	}

	/**
	 * Indicates whether the supplied {@code Component} is, in fact, a root
	 * Swing container.
	 *
	 * @param component the {@code Component} we wish to check
	 */
	public static boolean isValidRootContainer(Component component) {
		return component != null
				&& (component instanceof JFrame || component instanceof JApplet
				|| component instanceof JWindow || component instanceof JDialog);
	}

	public static RootWindow[] getVisibleWindows() {
		Frame[] frames = Frame.getFrames();
		HashSet<RootWindow> cache = new HashSet<>(frames.length);
		for (Frame frame : frames) {
			populateWindowList(new RootWindow(frame), cache, true);
		}
		return cache.toArray(new RootWindow[0]);
	}

	private static void populateWindowList(RootWindow win, HashSet<RootWindow> winCache,
										   boolean visOnly) {
		if (win == null || winCache.contains(win)) {
			return;
		}

		if (visOnly && !win.getRootContainer().isVisible()) {
			return;
		}

		winCache.add(win);
		Window[] children = win.getOwnedWindows();
		for (Window aChildren : children) {
			populateWindowList(new RootWindow(aChildren), winCache, visOnly);
		}
	}

	/**
	 * Creates a new {@code RootSwingContainer} wrapping the specified
	 * component.
	 */
	protected RootWindow(Component root) {
		setRootContainer(root);
		clientProperties = new HashMap<>();
	}

	/**
	 * Returns the {@code contentPane} object for the wrapped component.
	 *
	 * @return the {@code contentPane} property
	 */
	public Container getContentPane() {
		Container container = null;

		if (getRootContainer() instanceof RootPaneContainer) {
			container = ((RootPaneContainer) getRootContainer()).getContentPane();
		}

		return container;
	}

	/**
	 * Returns the {@code glassPane} object for the wrapped component.
	 *
	 * @return the {@code glassPane} property
	 */
	public Component getGlassPane() {
		Component component = null;

		if (getRootContainer() instanceof RootPaneContainer) {
			component = ((RootPaneContainer) getRootContainer()).getGlassPane();
		}

		return component;
	}

	/**
	 * Returns the {@code layeredPane} object for the wrapped component.
	 *
	 * @return the {@code layeredPane} property
	 */
	public JLayeredPane getLayeredPane() {
		JLayeredPane pane = null;

		if (getRootContainer() instanceof RootPaneContainer) {
			pane = ((RootPaneContainer) getRootContainer()).getLayeredPane();
		}

		return pane;
	}

	/**
	 * Returns the {@code LayoutManager} associated with {@code Component}
	 * maximization within the {@code RootSwingContainer}.
	 *
	 * @return a {@code LayoutManager} indicating the maximization layout
	 * property
	 * @deprecated dead code last used in 0.2.0
	 */
	public LayoutManager getMaximizedLayout() {
		return maxedLayout;
	}

	/**
	 * Returns the the wrapped component. ({@code JFrame}, {@code JApplet},
	 * etc...)
	 *
	 * @return the wrapped root container
	 */
	public Component getRootContainer() {
		return root.get();
	}

	/**
	 * Sets the {@code contentPane} property for the wrapped component.
	 *
	 * @param contentPane the {@code contentPane} object for the wrapped component
	 */
	public void setContentPane(Container contentPane) {
		if (getRootContainer() instanceof RootPaneContainer) {
			((RootPaneContainer) getRootContainer()).setContentPane(contentPane);
		}
	}

	/**
	 * Sets the {@code glassPane} property for the wrapped component.
	 *
	 * @param glassPane the {@code glassPane} object for the wrapped component
	 */
	public void setGlassPane(Component glassPane) {
		if (getRootContainer() instanceof RootPaneContainer) {
			((RootPaneContainer) getRootContainer()).setGlassPane(glassPane);
		}
	}

	/**
	 * Return an array containing all the windows this window currently owns.
	 *
	 * @return all the windows currently owned by this root window.
	 */
	private Window[] getOwnedWindows() {
		if (getRootContainer() instanceof JFrame) {
			return ((Window) getRootContainer()).getOwnedWindows();
		}
		else if (getRootContainer() instanceof JWindow) {
			return ((Window) getRootContainer()).getOwnedWindows();
		}
		else if (getRootContainer() instanceof JDialog) {
			return ((Window) getRootContainer()).getOwnedWindows();
		}
		else {
			return new Window[0];
		}
	}

	/**
	 * Sets the {@code LayoutManager} associated with {@code Component}
	 * maximization within the {@code RootSwingContainer}.
	 *
	 * @param mgr the {@code LayoutManager} associated with {@code Component}
	 *            maximization within the {@code RootSwingContainer}.
	 * @deprecated dead code last used in 0.2.0
	 */
	public void setMaximizedLayout(LayoutManager mgr) {
		maxedLayout = mgr;
	}

	/**
	 * Sets the wrapped root container.
	 *
	 * @param root the new wrapped root container
	 */
	private void setRootContainer(Component root) {
		this.root = new WeakReference<>(root);
	}

	public void updateComponentTreeUI() {
		SwingUtilities.updateComponentTreeUI(getRootContainer());
		pack();
	}

	private void pack() {
		Component root = getRootContainer();
		if (root instanceof JFrame) {
			((Window) root).pack();
		}
		else if (root instanceof JWindow) {
			((Window) root).pack();
		}
		else if (root instanceof JDialog) {
			((Window) root).pack();
		}
	}

	public void toFront() {
		Component root = getRootContainer();
		if (root instanceof JFrame) {
			((Window) root).toFront();
		}
		else if (root instanceof JWindow) {
			((Window) root).toFront();
		}
		else if (root instanceof JDialog) {
			((Window) root).toFront();
		}
	}

	public boolean isActive() {
		Component root = getRootContainer();
		if (root instanceof JFrame) {
			return ((Window) root).isActive();
		}
		else if (root instanceof JWindow) {
			return ((Window) root).isActive();
		}
		else if (root instanceof JDialog) {
			return ((Window) root).isActive();
		}
		return false;
	}

	public Window getOwner() {
		Component root = getRootContainer();
		if (root instanceof JFrame) {
			return ((Window) root).getOwner();
		}
		else if (root instanceof JWindow) {
			return ((Window) root).getOwner();
		}
		else if (root instanceof JDialog) {
			return ((Window) root).getOwner();
		}
		return null;
	}

	public Rectangle getBounds() {
		return getRootContainer().getBounds();
	}

	public void putClientProperty(Object key, Object value) {
		if (key == null) {
			return;
		}

		if (value == null) {
			clientProperties.remove(key);
		}
		else {
			clientProperties.put(key, value);
		}
	}

	public Object getClientProperty(Object key) {
		return key == null ? null : clientProperties.get(key);
	}
}
