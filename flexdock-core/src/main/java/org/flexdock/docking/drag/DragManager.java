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

import org.flexdock.docking.Dockable;
import org.flexdock.docking.DockingManager;
import org.flexdock.docking.DockingPort;
import org.flexdock.docking.DockingStrategy;
import org.flexdock.docking.drag.effects.EffectsManager;
import org.flexdock.docking.event.DockingEvent;
import org.flexdock.docking.floating.policy.FloatPolicyManager;
import org.flexdock.docking.state.FloatingGroup;
import org.flexdock.event.EventManager;
import org.flexdock.util.DockingUtility;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Christopher Butler
 */
public class DragManager extends MouseAdapter implements MouseMotionListener {
	private static final String DRAG_CONTEXT = "DragManager.DRAG_CONTEXT";
	private static final Object LOCK = new Object();
	private static DragOperation currentDragOperation;

	private Dockable dockable;
	private DragPipeline pipeline;
	private boolean enabled;
	private Point dragOrigin;
	private HashMap dragContext;
	
	private Point dragOffset = new Point();
	
	public static void prime() {
		// execute static initializer to preload resources
		EffectsManager.prime();
	}

	public DragManager(Dockable dockable) {
		this.dockable = dockable;
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (dockable == null || dockable.getDockingProperties().isDockingEnabled() == Boolean.FALSE) {
			enabled = false;
		}
		else {
			if (DockingUtility.isFloating(dockable)) {
				dragOffset = e.getPoint();
				// update the position of the floating frame/dockable
				String group = DockingUtility.getFloatGroup(dockable);
				
				FloatingGroup floatGroup = DockingManager.getFloatManager().getGroup(group);
				
				if (e.getSource() != floatGroup.getFrame()) {
					dragOffset = SwingUtilities.convertPoint((Component) e.getSource(), dragOffset, floatGroup.getFrame());
				}
			}
			
			toggleDragContext(true);
			enabled = !isDragCanceled(dockable, e);
		}
	}

	@Override
	public void mouseDragged(MouseEvent evt) {
		if (!enabled) {
			return;
		}

		if (dragOrigin == null) {
			dragOrigin = evt.getPoint();
		}

		if (pipeline == null || !pipeline.isOpen()) {
			if (passedDragThreshold(evt)) {
				System.out.println("isFloating: " + DockingUtility.isFloating(dockable));
				//if (!DockingUtility.isFloating(dockable)) {
				//DockingManager.getFloatManager().floatDockable(dockable, dockable.getComponent());
				////}
				//else {
				SwingUtilities.invokeLater(() -> openPipeline(evt));
				//}
			}
		}
		else {
			pipeline.processDragEvent(evt, dragOffset);
		}
	}

	private boolean passedDragThreshold(MouseEvent evt) {
		double distance = dragOrigin.distance(evt.getPoint());
		float threshold = dockable.getDockingProperties().getDragThreshold();
		return distance > threshold;
	}

	private void openPipeline(MouseEvent evt) {
		DragOperation token = new DragOperation(dockable, dragOrigin, evt);
		token.setDragListener(this);
		// initialize listeners on the drag-source
		initializeListenerCaching(token);

		DragPipeline pipeline = new DragPipeline();
		this.pipeline = pipeline;
		pipeline.open(token);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// doesn't do anything
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (pipeline == null || dockable.getDockingProperties().isDockingEnabled() == Boolean.FALSE) {
			return;
		}

		finishDrag(dockable, pipeline.getDragToken(), e);
		if (pipeline != null) {
			pipeline.close();
		}
		toggleDragContext(false);
		dragOrigin = null;
		pipeline = null;
	}


	private void finishDrag(Dockable dockable, DragOperation token, MouseEvent mouseEvt) {
		DockingStrategy docker = DockingManager.getDockingStrategy(dockable);
		DockingPort currentPort = DockingUtility.getParentDockingPort(dockable);
		DockingPort targetPort = token.getTargetPort();
		String region = token.getTargetRegion();

		// remove the listeners from the drag-source and all the old ones back in
		restoreCachedListeners(token);

		// issue a DockingEvent to allow any listeners the chance to cancel the operation.
		DockingEvent evt = new DockingEvent(dockable, currentPort, targetPort, DockingEvent.DROP_STARTED, mouseEvt, getDragContext());
		evt.setRegion(region);
		evt.setOverWindow(token.isOverWindow());
		EventManager.dispatch(evt, dockable);
		
		// attempt to complete the docking operation
		if (!evt.isConsumed()) {
			docker.dock(dockable, targetPort, region, token);
		}
	}


	private static void initializeListenerCaching(DragOperation token) {
		// it's easier for us if we remove the MouseMostionListener associated with the dragSource
		// before dragging, so normally we'll try to do that.  However, if developers really want to
		// keep them in there, then they can implement the Dockable interface for their dragSource and
		// let mouseMotionListenersBlockedWhileDragging() return false
//                if (!dockableImpl.mouseMotionListenersBlockedWhileDragging())
//                        return;

		Component dragSrc = token.getDragSource();
		EventListener[] cachedListeners = dragSrc.getListeners(MouseMotionListener.class);
		token.setCachedListeners(cachedListeners);
		DragManager dragListener = token.getDragListener();

		// remove all of the MouseMotionListeners
		for (EventListener cachedListener : cachedListeners) {
			dragSrc.removeMouseMotionListener((MouseMotionListener) cachedListener);
		}
		// then, re-add the DragManager
		if (dragListener != null) {
			dragSrc.addMouseMotionListener(dragListener);
		}
	}

	private static void restoreCachedListeners(DragOperation token) {
		Component dragSrc = token.getDragSource();
		EventListener[] cachedListeners = token.getCachedListeners();
		DragManager dragListener = token.getDragListener();

		// remove the pipeline listener
		if (dragListener != null) {
			dragSrc.removeMouseMotionListener(dragListener);
		}

		// now, re-add all of the original MouseMotionListeners
		for (EventListener cachedListener : cachedListeners) {
			dragSrc.addMouseMotionListener((MouseMotionListener) cachedListener);
		}
	}

	private static boolean isDragCanceled(Dockable dockable, MouseEvent trigger) {
		System.out.println("isDragCanceled");
		DockingPort port = DockingUtility.getParentDockingPort(dockable);
		Map dragContext = getDragContext(dockable);
		DockingEvent evt = new DockingEvent(dockable, port, null, DockingEvent.DRAG_STARTED, trigger, dragContext);
		EventManager.dispatch(evt, dockable);
		System.out.println("canceled: " + evt.isConsumed());
		return evt.isConsumed();
	}

	public static Map getDragContext(Dockable dockable) {
		Object obj = dockable == null ? null : dockable.getClientProperty(DRAG_CONTEXT);
		return obj instanceof Map ? (Map) obj : null;
	}

	private void toggleDragContext(boolean add) {
		if (add) {
			if (dragContext == null) {
				dragContext = new HashMap();
				dockable.putClientProperty(DRAG_CONTEXT, dragContext);
			}
		}
		else {
			if (dragContext != null) {
				dragContext.clear();
				dragContext = null;
			}
			dockable.putClientProperty(DRAG_CONTEXT, null);
		}
	}

	private Map getDragContext() {
		return getDragContext(dockable);
	}

	public static boolean isFloatingAllowed(Dockable dockable) {
		return FloatPolicyManager.isFloatingAllowed(dockable);
	}

	public static DragOperation getCurrentDragOperation() {
		synchronized (LOCK) {
			return currentDragOperation;
		}
	}

	static void setCurrentDragOperation(DragOperation operation) {
		synchronized (LOCK) {
			currentDragOperation = operation;
		}
	}


}
