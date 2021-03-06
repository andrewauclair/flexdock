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
package org.flexdock.view.dockbar.layout;

import org.flexdock.util.RootWindow;
import org.flexdock.view.dockbar.DockbarManager;

import javax.swing.*;
import java.awt.*;

/**
 * @author Christopher Butler
 */
public class DockbarLayoutManager {
	private static final Object LOCK = new Object();
	private static final DockbarLayoutManager DEFAULT_INSTANCE = new DockbarLayoutManager();
	private static DockbarLayoutManager viewAreaManager = DEFAULT_INSTANCE;

	public static DockbarLayoutManager getManager() {
		synchronized (LOCK) {
			return viewAreaManager;
		}
	}

	public static void setManager(DockbarLayoutManager mgr) {
		synchronized (LOCK) {
			viewAreaManager = mgr == null ? DEFAULT_INSTANCE : mgr;
		}
	}

	public Rectangle getViewArea(DockbarManager mgr) {
		if (mgr == null) {
			return new Rectangle(0, 0, 0, 0);
		}

		Rectangle leftBar = mgr.getLeftBar().getBounds();
		Rectangle bottomBar = mgr.getBottomBar().getBounds();
		Rectangle rightBar = mgr.getRightBar().getBounds();
		return new Rectangle(leftBar.x + leftBar.width, leftBar.y, bottomBar.width - leftBar.width - rightBar.width, leftBar.height);
	}

	public Rectangle getLayoutArea(DockbarManager mgr) {
		Rectangle rect = new Rectangle();
		RootWindow window = mgr == null ? null : mgr.getWindow();
		if (window == null) {
			return rect;
		}

		JLayeredPane layeredPane = window.getLayeredPane();

		Component leftEdge = getEdgeGuide(mgr, SwingConstants.LEFT);
		Component rightEdge = getEdgeGuide(mgr, SwingConstants.RIGHT);
		Component bottomEdge = getEdgeGuide(mgr, SwingConstants.BOTTOM);
		Component topEdge = getEdgeGuide(mgr, SwingConstants.TOP);


		Rectangle leftBounds = SwingUtilities.convertRectangle(leftEdge.getParent(), leftEdge.getBounds(), layeredPane);
		Rectangle rightBounds = SwingUtilities.convertRectangle(rightEdge.getParent(), rightEdge.getBounds(), layeredPane);
		Rectangle bottomBounds = SwingUtilities.convertRectangle(bottomEdge.getParent(), bottomEdge.getBounds(), layeredPane);
		Rectangle topBounds = SwingUtilities.convertRectangle(topEdge.getParent(), topEdge.getBounds(), layeredPane);

		int rightX = rightBounds.x + rightBounds.width;
		int bottomY = bottomBounds.y + bottomBounds.height;

		//TODO: There is some a flaw we're not accounting for here.  We're assuming that
		// with the various different edge-guide components we're using, the leftEdge will
		// actually be to the left, rightEdge will actually be to the right, and so on.
		// If the user does something unreasonable like specify a rightEdge component that is
		// actually to the left of their leftEdge, then we're going to end up with some wacky,
		// unpredictable results.

		rect.x = leftBounds.x;
		rect.y = topBounds.y;
		rect.width = rightX - rect.x;
		rect.height = bottomY - rect.y;
		return rect;
	}

	public JComponent getEdgeGuide(DockbarManager mgr, int edge) {
		// default behavior is to return the contentPane for all edges
		RootWindow window = mgr == null ? null : mgr.getWindow();
		Component comp = window == null ? null : window.getContentPane();
		return comp instanceof JComponent ? (JComponent) comp : null;
	}
}
