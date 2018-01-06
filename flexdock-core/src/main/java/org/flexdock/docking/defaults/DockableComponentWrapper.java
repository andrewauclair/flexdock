/*
 * Copyright (c) 2004 Christopher M Butler
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.flexdock.docking.defaults;

import org.flexdock.docking.Dockable;
import org.flexdock.docking.DockingManager;
import org.flexdock.docking.DockingPort;
import org.flexdock.docking.DockingStub;
import org.flexdock.docking.adapter.DockingAdapter;
import org.flexdock.docking.event.DockingEvent;
import org.flexdock.docking.event.DockingListener;
import org.flexdock.docking.props.DockablePropertySet;
import org.flexdock.docking.props.PropertyManager;
import org.flexdock.util.SwingUtility;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.List;

/**
 * This class models a {@code Dockable} implementation for wrapping a
 * {@code Component}. It is essentially the simplest means to turning a generic
 * {@code Component} into a {@code Dockable} instance. Compound
 * {@code Dockables} may have separate child components that are responsible for
 * drag initiation, whereas another component is the actual drag source. This is
 * shown in the manner that a {@code JInternalFrame} would be a draggable
 * component, while the frame's title pane is the actual drag initiator.
 * <p>
 * The class, conversely, deals with the <i>simple</i> case, where a
 * {@code Component} itself must be docking-enabled.
 * {@code DockableComponentWrapper} wraps a {@code Component} and implements the
 * {@code Dockable} interface. Since the {@code Component} itself is being
 * docking-enabled, it serves as both the drag source and drag initiator. Thus,
 * {@code getComponent()} will return a reference to {@code 'this'} and
 * {@code getDragSources()} return a {@code List} containing the same
 * self-reference {@code Component}.
 * <p>
 * This class may be used by application code to enable docking capabilities on
 * a given {@code Component}. However, it is recommended that
 * {@code DockingManager.registerDockable(Component evtSrc, String desc)} be
 * used as a more automated, less invasive means of enabling docking on a
 * component.
 * {@code DockingManager.registerDockable(Component evtSrc, String desc)} will
 * automatically create a {@code DockableComponentWrapper} instance and register
 * the required drag listeners.
 *
 * @author Chris Butler
 */
public class DockableComponentWrapper extends AbstractDockable {
	private Component dragSrc;

	/**
	 * Creates a {@code DockableComponentWrapper} instance using the specified
	 * source component, persistent ID, and docking description. This method is
	 * used to create {@code Dockable} instances for simple {@code Components}
	 * where the drag source and drag initiator are the same {@code Component}.
	 * <p>
	 * If {@code src} or {@code id} are {@code null}, then this method returns
	 * a {@code null} reference.
	 * <p>
	 * {@code src} will be the {@code Component} returned by invoking
	 * {@code getComponent()} on the resulting {@code Dockable} and will be
	 * included in the {@code List} returned by {@code getDragSources()}.
	 * {@code id} will be the value returned by invoking
	 * {@code getPersistentId()} on the resulting {@code Dockable}.
	 * {@code desc} may be used by the {@code Dockable} for descriptive purposes
	 * (such as tab-text in a tabbed layout). It is not recommended to supply a
	 * {@code null} value for {@code desc}, but doing so is not illegal.
	 *
	 * @param src  the source component
	 * @param id   the persistent ID for the Dockable instance
	 * @param desc the docking description
	 * @return a new {@code DockableComponentWrapper} instance
	 * @see Dockable#getComponent()
	 * @see Dockable#getDragSources()
	 * @see Dockable#getPersistentId()
	 * @see DockingManager#registerDockable(Component, String)
	 */
	public static DockableComponentWrapper create(Component src, String id,
												  String desc) {
		if (src == null || id == null) {
			return null;
		}

		return new DockableComponentWrapper(src, id, desc);
	}

	public static DockableComponentWrapper create(DockingStub stub) {
		if (!(stub instanceof Component)) {
			return null;
		}

		return create((Component) stub, stub.getPersistentId(), stub
				.getTabText());
	}

	public static DockableComponentWrapper create(DockingAdapter adapter) {
		if (adapter == null) {
			return null;
		}

		Component comp = adapter.getComponent();
		String id = adapter.getPersistentId();
		String tabText = adapter.getTabText();
		DockableComponentWrapper dockable = create(comp, id, tabText);

		List<Component> dragSources = adapter.getDragSources();
		Set<Component> frameDragSources = adapter.getFrameDragSources();
		Icon icon = adapter.getDockbarIcon();

		if (dragSources != null) {
			dockable.getDragSources().clear();
			dockable.getDragSources().addAll(dragSources);
		}

		if (frameDragSources != null) {
			dockable.getFrameDragSources().clear();
			dockable.getFrameDragSources().addAll(frameDragSources);
		}

		if (icon != null) {
			dockable.getDockingProperties().setDockbarIcon(icon);
		}

		return dockable;
	}

	public <T extends Component & DockingStub> DockableComponentWrapper(T dockable) {
		super(dockable.getPersistentId());
		
		dragSrc = dockable;
		getDockingProperties().setDockableDesc(dockable.getTabText());

		initDragListeners();
	}

	private DockableComponentWrapper(Component src, String id, String desc) {
		super(id);
		
		dragSrc = src;
		getDockingProperties().setDockableDesc(desc);

		// initialize the drag sources lists
		initDragListeners();
	}

	private void initDragListeners() {
		// by default, use the wrapped source component as the drag source
		// and assume there is no frame drag source defined
		Component draggable = dragSrc;
		Component frameDragger = null;

		// if the wrapped source component is a DockingStub, then
		// we'll be able to pull some extra data from it
		if (dragSrc instanceof DockingStub) {
			DockingStub stub = (DockingStub) dragSrc;
			Component c = stub.getDragSource();
			// if the stub defines a specific drag source, then
			// replace wrapped source component with the specified
			// drag source
			if (c != null) {
				draggable = c;
			}
			// if the stub defines a specified frame drag source, then
			// use it
			frameDragger = stub.getFrameDragSource();
		}

		// add the "docking" drag source to the list
		if (draggable != null) {
			//dragListeners.add(draggable);
		}

		// add the floating frame drag source to the list
		if (frameDragger != null) {
			getFrameDragSources().add(frameDragger);
		}
	}

	/**
	 * Returns the {@code Component} used to create this
	 * {@code DockableComponentWrapper} instance.
	 *
	 * @return the {@code Component} used to create this
	 * {@code DockableComponentWrapper} instance.
	 * @see Dockable#getComponent()
	 * @see #create(Component, String, String)
	 */
	@Override
	public Component getComponent() {
		return dragSrc;
	}
}
