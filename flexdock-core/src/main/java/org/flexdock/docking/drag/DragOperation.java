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
import org.flexdock.docking.state.FloatingGroup;
import org.flexdock.util.DockingUtility;
import org.flexdock.util.SwingUtility;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.EventListener;

import static org.flexdock.docking.DockingConstants.UNKNOWN_REGION;

public class DragOperation {
	public static final String DRAG_IMAGE = "DragOperation.DRAG_IMAGE";

	private Component dragSource;
	private Dockable dockable;
	private Point mouseOffset;
	private Point currentMouse;
	private EventListener[] cachedListeners;
	private DragManager dragListener;
	private DockingPort targetPort;
	private String targetRegion;
	private boolean overWindow;
	private boolean pseudoDrag;
	private long started;
	private Dockable dockableRef;
	private DockingPort sourcePort;
	
	
	DragOperation(Dockable dockable, Point dragOrigin, MouseEvent evt) {
		if (dockable == null) {
			throw new NullPointerException("'dockable' parameter cannot be null.");
		}
		if (evt == null) {
			throw new NullPointerException("'evt' parameter cannot be null.");
		}
		if (!(evt.getSource() instanceof Component)) {
			throw new IllegalArgumentException("'evt.getSource()' must be an instance of java.awt.Component.");
		}

		init(dockable, (Component) evt.getSource(), dragOrigin == null ? evt.getPoint() : dragOrigin);
	}
	
	private void init(Dockable dockable, Component dragSource, Point currentMouse) {
		this.dockable = dockable;
		this.dragSource = dragSource;
		this.currentMouse = currentMouse;
		mouseOffset = calculateMouseOffset(currentMouse);
		pseudoDrag = false;

		sourcePort = DockingManager.getDockingPort(dockable);
		started = -1;
	}

	private Point calculateMouseOffset(Point evtPoint) {
		if (evtPoint == null) {
			return null;
		}
		
		if (dockable.getComponent().isVisible()) {
			Point dockableLoc = dockable.getComponent().getLocationOnScreen();
			SwingUtilities.convertPointToScreen(evtPoint, dragSource);
			Point offset = new Point();
			offset.x = dockableLoc.x - evtPoint.x;
			offset.y = dockableLoc.y - evtPoint.y;
			return offset;
		}

		return null;
	}

	public Component getDockable() {
		return dockable.getComponent();
	}

	public Dockable getDockableReference() {
		if (dockableRef == null) {
			dockableRef = DockingManager.getDockable(dockable.getComponent());
		}
		return dockableRef;
	}

	public Point getMouseOffset() {
		return (Point) mouseOffset.clone();
	}
	
	public void updateMouse(MouseEvent me, Point dragOffset) {
		if (me != null && me.getSource() == dragSource) {
			currentMouse = me.getPoint();
			
			if (DockingUtility.isFloating(dockable)) {
				// update the position of the floating frame/dockable
				String group = DockingUtility.getFloatGroup(dockable);
				
				FloatingGroup floatGroup = DockingManager.getFloatManager().getGroup(group);
				
				// TODO This needs to use the dragOffset in FrameDragListener
				Point loc = me.getPoint();
				SwingUtilities.convertPointToScreen(loc, (Component) me.getSource());
				SwingUtility.subtract(loc, dragOffset);
				floatGroup.getFrame().setLocation(loc);
				
			}
		}
	}

	public Point getCurrentMouse(boolean relativeToScreen) {
		Point p = (Point) currentMouse.clone();
		if (relativeToScreen) {
			SwingUtilities.convertPointToScreen(p, dragSource);
		}
		return p;
	}

	public Rectangle getDragRect(boolean relativeToScreen) {
		Point p = getCurrentMouse(relativeToScreen);
		Point offset = getMouseOffset();
		p.x += offset.x;
		p.y += offset.y;

		Rectangle r = new Rectangle(getDragSize());
		r.setLocation(p);
		return r;

	}

	public Point getCurrentMouse(Component target) {
		if (target == null || !target.isVisible()) {
			return null;
		}
		return SwingUtilities.convertPoint(dragSource, currentMouse, target);
	}

	private Dimension getDragSize() {
		return dockable.getComponent().getSize();
	}

	public Component getDragSource() {
		return dragSource;
	}

	public void setTarget(DockingPort port, String region) {
		targetPort = port;
		targetRegion = region == null ? UNKNOWN_REGION : region;
	}

	public DockingPort getTargetPort() {
		return targetPort;
	}

	public String getTargetRegion() {
		return targetRegion;
	}

	public EventListener[] getCachedListeners() {
		return cachedListeners == null ? new EventListener[0] : cachedListeners;
	}

	public void setCachedListeners(EventListener[] listeners) {
		cachedListeners = listeners;
	}

	public DragManager getDragListener() {
		return dragListener;
	}

	public void setDragListener(DragManager listener) {
		this.dragListener = listener;
	}

	public boolean isOverWindow() {
		return overWindow;
	}

	public void setOverWindow(boolean overWindow) {
		this.overWindow = overWindow;
	}

	public boolean isPseudoDrag() {
		return pseudoDrag;
	}

	public void start() {
		if (started == -1) {
			started = System.currentTimeMillis();
		}
	}

	public long getStartTime() {
		return started;
	}

	public DockingPort getSourcePort() {
		return sourcePort;
	}
}