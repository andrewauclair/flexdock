/*
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
package org.flexdock.docking.drag;

import org.flexdock.docking.DockingConstants;
import org.flexdock.docking.DockingPort;
import org.flexdock.util.RootWindow;
import org.flexdock.util.SwingUtility;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.HashMap;

public class DragPipeline {

	private GlassPaneMonitor paneMonitor;
	private RootWindow[] windows;
	private HashMap<Rectangle, RootWindow> rootWindowsByBounds;
	private DragGlasspane currentGlasspane;
	private DragGlasspane newGlassPane;
	private Rectangle[] windowBounds;
	private boolean heavyweightDockableSupportted;

	private boolean open;
	private DragOperation dragToken;

	DragPipeline() {
		paneMonitor = new GlassPaneMonitor();
	}

	public boolean isOpen() {
		return open;
	}

	public void open(DragOperation token) {
		if (token == null) {
			throw new NullPointerException("'token' parameter cannot be null.");
		}

		if (EventQueue.isDispatchThread()) {
			openImpl(token);
			return;
		}


		final DragOperation dToken = token;
		try {
			EventQueue.invokeAndWait(() -> openImpl(dToken));
		}
		catch (Exception e) {
			System.err.println("Exception: " + e.getMessage());
		}
	}

	private void openImpl(DragOperation operation) {
		// check to see if we're going to support heavyweight dockables for this operation
		heavyweightDockableSupportted = Boolean.getBoolean(DockingConstants.HEAVYWEIGHT_DOCKABLES);

		this.dragToken = operation;

		// turn the current drag operation on
		setCurrentDragOperation(operation);

		// TODO We want to ignore the preview here
		windows = Arrays.stream(RootWindow.getVisibleWindows())
				.filter(p -> p.getBounds().width != 200 && p.getBounds().height != 200)
				.toArray(RootWindow[]::new);

		windowBounds = new Rectangle[windows.length];
		rootWindowsByBounds = new HashMap<>();

		for (int i = 0; i < windows.length; i++) {
			RootWindow window = windows[i];
			applyGlassPane(window, createGlassPane());
			windowBounds[i] = window.getBounds();
			rootWindowsByBounds.put(windowBounds[i], window);
		}

		operation.start();
		open = true;
	}

	private DragGlasspane createGlassPane() {
		DragGlasspane pane = new DragGlasspane();
		pane.addMouseListener(paneMonitor);
		return pane;
	}

	private void applyGlassPane(RootWindow win, DragGlasspane pane) {
		pane.setRootWindow(win);
		pane.setCachedGlassPane(win.getGlassPane());
		win.setGlassPane(pane);
		pane.setVisible(true);
	}

	public void close() {
		if (!open) {
			return;
		}

		for (int i = 0; i < windows.length; i++) {
			Component cmp = windows[i].getGlassPane();
			if (cmp instanceof DragGlasspane) {
				DragGlasspane pane = (DragGlasspane) cmp;
				pane.setVisible(false);
				cmp = pane.getCachedGlassPane();

				windows[i].setGlassPane(cmp);
				windows[i] = null;
			}
		}

		windowBounds = null;
		rootWindowsByBounds.clear();
		// turn the current drag operation off
		setCurrentDragOperation(null);
		open = false;
	}

	public void processDragEvent(MouseEvent me, Point dragOffset) {
		if (!open) {
			return;
		}

		if (EventQueue.isDispatchThread()) {
			processDragEventImpl(me, dragOffset);
			return;
		}

		EventQueue.invokeLater(() -> processDragEventImpl(me, dragOffset));
	}

	private void processDragEventImpl(MouseEvent me, Point dragOffset) {

		dragToken.updateMouse(me, dragOffset);

		if (heavyweightDockableSupportted) {
			preprocessHeavyweightDockables();
		}

		me.consume();

		// track whether or not we're currently over a window
		dragToken.setOverWindow(newGlassPane != null);

		// if the glasspane hasn't changed, then reprocess on the current glasspane
		if (newGlassPane == currentGlasspane) {
			dontSwitchGlassPanes();
			return;
		}

		// process transitions from a glasspane to a null area
		if (newGlassPane == null) {
			transitionToNullArea();
			return;
		}

		// process transitions from null area to a glasspane
		if (currentGlasspane == null) {
			transitionFromNullArea(newGlassPane);
			return;
		}

		// otherwise, transition from one glasspane to another
		// clear out the old glasspane
		currentGlasspane.clear();
		// reassign to the new glasspane
		currentGlasspane = newGlassPane;
		// now process the new glasspane and redraw the rubberband
		currentGlasspane.processDragEvent(dragToken);
	}

	private void dontSwitchGlassPanes() {
		// just redraw the rubberband if there's no current glasspane
		if (currentGlasspane == null) {
			return;
		}

		// otherwise, process the drag event on the current glasspane
		// and repaint it.
		// TODO: Fix post-painter on unchanged glasspane.
//                currentGlasspane.setPostPainter(getPostPainter(screenRect));
		currentGlasspane.processDragEvent(dragToken);
	}

	private void transitionToNullArea() {
		// set the new glasspane reference
		DragGlasspane pane = currentGlasspane;
		currentGlasspane = null;

		// clear out the old glasspane and redraw the rubberband
		pane.clear();
	}

	private void transitionFromNullArea(DragGlasspane newGlassPane) {
		// set the new glasspane reference
		currentGlasspane = newGlassPane;

		// process the new glasspane
		currentGlasspane.processDragEvent(dragToken);
	}


	private void setCurrentGlassPane(DragGlasspane gp) {
		newGlassPane = gp;
	}


	private class GlassPaneMonitor extends MouseAdapter {
		@Override
		public void mouseEntered(MouseEvent me) {
			// TODO If position is within the preview frame then ignore this entered event
//            System.out.println("mouseEntered: " + me.getComponent().getName());
//		    // TODO Make sure this isn't the preview
//
//            if (me.getComponent() instanceof JFrame) {
//                JFrame frame = (JFrame) me.getComponent();
//                if (frame.getTitle().equals("Preview")) {
//                    System.out.println("Skip mouseEntered on Preview");
//                    return;
//                }
//            }
			Object obj = me.getSource();
			if (obj instanceof DragGlasspane) {
				setCurrentGlassPane((DragGlasspane) obj);
			}
		}

		@Override
		public void mouseExited(MouseEvent me) {
			// TODO If position is within the preview frame, then ignore this exited event
//		    System.out.println("mouseExited: " + me.getComponent().getName());
//
//		    // TODO Make sure this isn't the preview
//			if (me.getComponent() instanceof JFrame) {
//			    JFrame frame = (JFrame) me.getComponent();
//			    if (frame.getTitle().equals("Preview")) {
//			        System.out.println("Skip mouseExited on Preview");
//			        return;
//                }
//            }
			setCurrentGlassPane(null);
		}
	}

	public DragOperation getDragToken() {
		return dragToken;
	}

	private void setCurrentDragOperation(DragOperation operation) {
		DragOperation current = DragManager.getCurrentDragOperation();
		if (operation == current) {
			return;
		}

		DockingPort srcPort = operation == null ? current.getSourcePort() : operation.getSourcePort();
		DragManager.setCurrentDragOperation(operation);
		if (srcPort instanceof Component) {
			// TODO Why is this forcing a repaint?
			SwingUtility.repaint((Component) srcPort);
		}
	}

	private void preprocessHeavyweightDockables() {
		RootWindow targetWindow = getTargetWindow();

		if (newGlassPane == null && targetWindow != null) {
			Component gp = targetWindow.getGlassPane();
			if (gp instanceof DragGlasspane) {
				setCurrentGlassPane((DragGlasspane) gp);
			}
		}
	}

	private RootWindow getTargetWindow() {
		Point screenLoc = dragToken.getCurrentMouse(true);
		for (Rectangle windowBound : windowBounds) {
			if (windowBound.contains(screenLoc)) {
				return rootWindowsByBounds.get(windowBound);
			}
		}
		return null;
	}
}
