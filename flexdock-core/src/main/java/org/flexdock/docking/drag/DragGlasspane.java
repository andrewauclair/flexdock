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
import org.flexdock.docking.DockingConstants;
import org.flexdock.docking.DockingManager;
import org.flexdock.docking.DockingPort;
import org.flexdock.docking.drag.effects.DragPreview;
import org.flexdock.docking.drag.effects.EffectsManager;
import org.flexdock.util.NestedComponents;
import org.flexdock.util.RootWindow;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

@SuppressWarnings(value = {"serial"})
public class DragGlasspane extends JComponent {
	
	private NestedComponents currentDropTargets;
	private Component cachedGlassPane;
	private RootWindow rootWindow;
	private DragPreview previewDelegate;
	private boolean previewInit;
	private Polygon previewPoly;
	private DragOperation currentDragToken;
	
	DragGlasspane() {
		setLayout(null);
	}
	
	public Component getCachedGlassPane() {
		return cachedGlassPane;
	}

    public void setCachedGlassPane(Component cachedGlassPane) {
		this.cachedGlassPane = cachedGlassPane;
	}
	
	public void setRootWindow(RootWindow rootWindow) {
		this.rootWindow = rootWindow;
	}
	
	private NestedComponents getDropTargets(DragOperation token) {
		Container c = rootWindow.getContentPane();
		Point currMouse = token.getCurrentMouse(c);
		Component deep = SwingUtilities.getDeepestComponentAt(c, currMouse.x, currMouse.y);
//		Component deep = getDeepestComponentAt(c, currMouse.x, currMouse.y);
		return NestedComponents.find(deep, Dockable.class, DockingPort.class);
	}

    public void processDragEvent(DragOperation token) {
		currentDragToken = token;
		NestedComponents dropTargets = getDropTargets(token);
		
		// if there is no cover, and we're not transitioning away from one,
		// then invoke postPaint() and return
		if (currentDropTargets == null && dropTargets == null) {
			return;
		}

		DockingConstants.Region region;
		
		// now, assign the currentCover to the new one and repaint
		currentDropTargets = dropTargets;
		DockingPort port = dropTargets == null ? null : (DockingPort) dropTargets.parent;
		// this is the dockable we're currently hovered over, not the one
		// being dragged
		Dockable hover = getHoverDockable(dropTargets);

		Point mousePoint = token.getCurrentMouse((Component) port);

		region = findRegion(port, hover, mousePoint);

		// set the target dockable
		token.setTarget(port, region);
		
		// create the preview-polygon
		createPreviewPolygon(token, port, hover, region);
		
		// repaint
		repaint();
	}

	private DockingConstants.Region findRegion(DockingPort hoverPort, Dockable hoverDockable, Point mousePoint) {
		if (hoverPort == null) {
			return null;
		}
		
		if (hoverDockable != null) {
			return hoverPort.getRegion(mousePoint);
		}
		
		// apparently, we're not hovered over a valid dockable.  either the dockingport
		// is empty, or it already contains a non-dockable component.  if it's empty, then
		// we can dock into it.  otherwise, we need to short-circuit the docking operation.
		Component docked = hoverPort.getDockedComponent();
		// if 'docked' is null, then the port is empty and we can dock
		if (docked == null) {
			return hoverPort.getRegion(mousePoint);
		}
		
		// the port contains a non-dockable component.  we can't dock
		return null;
	}
	
	private Dockable getHoverDockable(NestedComponents nest) {
		Component c = nest == null ? null : nest.child;
		if (c instanceof Dockable) {
			return (Dockable) c;
		}
		if (c == null) {
			return null;
		}
		return DockingManager.getDockable(c);
	}

	private void createPreviewPolygon(DragOperation token, DockingPort port, Dockable hover, DockingConstants.Region region) {
		DragPreview preview = getPreviewDelegate();
		if (preview == null) {
			previewPoly = null;
		}
		else {
			Map dragContext = getDragContext(token);
			previewPoly = preview.createPreviewPolygon(token.getDockable(), port, hover, region, this, dragContext);
		}
	}
	
	public void clear() {
		if (currentDropTargets != null) {
			currentDropTargets = null;
		}
		repaint();
	}
	
	@Override
	public void paint(Graphics g) {
		paintComponentImpl(g);
	}

	private DragPreview getPreviewDelegate() {
		if (!previewInit) {
			previewDelegate = EffectsManager.getPreview();
			previewInit = true;
		}
		return previewDelegate;
	}
	
	private void paintComponentImpl(Graphics g) {
		if (currentDragToken != null && previewDelegate != null && previewPoly != null) {
			Dockable dockable = currentDragToken.getDockableReference();
			Map dragInfo = getDragContext(currentDragToken);
			previewDelegate.drawPreview((Graphics2D) g, previewPoly, dockable, dragInfo);
		}
	}
	
	private Map getDragContext(DragOperation token) {
		if (token == null) {
			return null;
		}
		
		Dockable dockable = token.getDockableReference();
		return DragManager.getDragContext(dockable);
	}
}
