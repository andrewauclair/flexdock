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
package org.flexdock.docking.drag.effects;

import org.flexdock.docking.Dockable;
import org.flexdock.docking.DockingPort;
import org.flexdock.docking.RegionChecker;
import org.flexdock.docking.defaults.DefaultRegionChecker;
import org.flexdock.util.SwingUtility;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

import static org.flexdock.docking.DockingConstants.Region;

/**
 * TODO I want to merge this with the AlphaPreview because that is going to be the one and only preview.
 * @author Christopher Butler
 */
public abstract class DefaultPreview implements DragPreview {
	private static final int DEFAULT_TAB_WIDTH = 50;
	private static final int DEFAULT_TAB_HEIGHT = 20;

	@Override
	public Polygon createPreviewPolygon(Component dockable, DockingPort port, Dockable hover, Region targetRegion, Component paintingTarget, Map dragInfo) {
		if (dockable == null || port == null || targetRegion == null || paintingTarget == null) {
			return null;
		}

		if (!port.isDockingAllowed(dockable, targetRegion)) {
			return null;
		}

		// if we're not hovering over another Dockable then the DockingPort we're over is empty.
		// return its bounds.
		if (hover == null) {
			Rectangle portBounds = ((Component) port).getBounds();
			return createPolyRect(portBounds);
		}

		Polygon p;
		Component srcAxes = hover.getComponent();
		if (isOuterRegion(targetRegion)) {
			p = createPolyRect(port, srcAxes, targetRegion);
		}
		else {
			p = createPolyTab(port, srcAxes);
			srcAxes = (Component) port;
		}

		SwingUtility.translate(srcAxes, p, paintingTarget);
		return p;
	}

	private Polygon createPolyRect(DockingPort port, Component dockable, Region region) {
		RegionChecker regionChecker = port.getDockingProperties().getRegionChecker();
		if (regionChecker == null) {
			regionChecker = new DefaultRegionChecker();
		}

		Rectangle r = regionChecker.getSiblingBounds(dockable, region);
		return createPolyRect(r);
	}

	private Polygon createPolyRect(Rectangle r) {
		if (r == null) {
			return null;
		}

		int x2 = r.x + r.width;
		int y2 = r.y + r.height;
		int[] x = new int[]{r.x, x2, x2, r.x};
		int[] y = new int[]{r.y, r.y, y2, y2};
		return new Polygon(x, y, 4);
	}


	private Polygon createPolyTab(DockingPort port, Component hover) {
		Component c = port.getDockedComponent();

		Rectangle tabPaneRect = createTabbedPaneRect(port, hover);
		// if no existing component and no singleTabs allowed,
		// return the entire pane bounds
		if (c == null && port.getDockingProperties().isSingleTabsAllowed() == Boolean.FALSE) {
			return createPolyRect(tabPaneRect);
		}

		Rectangle tabRect = new Rectangle(tabPaneRect.x, tabPaneRect.y, DEFAULT_TAB_WIDTH, DEFAULT_TAB_HEIGHT);
		boolean tabsOnTop = port.getDockingProperties().getTabPlacement() == JTabbedPane.TOP;
		// if 'c' is a JTabbedPane, then there is already a tab out there and
		// we can model our bounds off of it.
		if (c instanceof JTabbedPane) {
			JTabbedPane tabs = (JTabbedPane) c;
			Rectangle lastTab = tabs.getBoundsAt(tabs.getTabCount() - 1);
			tabRect.height = lastTab.height;
			tabRect.y = lastTab.y;
			tabRect.x = lastTab.x + lastTab.width;
			tabsOnTop = tabs.getTabPlacement() == JTabbedPane.TOP;
		}
		else {
			tabRect.y = tabsOnTop ? 0 : tabPaneRect.height - DEFAULT_TAB_HEIGHT;
			// if there is already a component in the docking port, then our new
			// component will be dropped into the second tab, not the first
			if (c != null) {
				tabRect.x += DEFAULT_TAB_WIDTH;
			}
		}

		// subtract tab height from the pane-rect height, and shift its location
		// down if the tab sits on top
		tabPaneRect.height -= tabRect.height;
		if (tabsOnTop) {
			tabPaneRect.y += tabRect.height;
		}

		if (tabsOnTop) {
			return createPolyTabOnTop(tabPaneRect, tabRect);
		}
		else {
			return createPolyTabOnBottom(tabPaneRect, tabRect);
		}
	}

	protected Rectangle createTabbedPaneRect(DockingPort port, Component hover) {
		// get the bounds and reset location to (0, 0), since we'll be
		// converting coordinates from the DockingPort, not its parent
		Rectangle tabPaneRect = ((Component) port).getBounds();
		tabPaneRect.setLocation(0, 0);
		return tabPaneRect;
	}

	private Polygon createPolyTabOnTop(Rectangle tabPane, Rectangle tab) {
		Polygon p = new Polygon();
		int tabRight = tab.x + tab.width;
		int paneRight = tabPane.x + tabPane.width;
		int paneBottom = tabPane.y + tabPane.height;

		// if the tab isn't at the origin, then build the path
		// until we reach the tab
		if (tab.x != 0) {
			p.addPoint(tabPane.x, tabPane.y);
			p.addPoint(tab.x, tabPane.y);
		}
		p.addPoint(tab.x, tab.y);
		p.addPoint(tabRight, tab.y);
		p.addPoint(tabRight, tabPane.y);
		p.addPoint(paneRight, tabPane.y);

		// create the right-side
		p.addPoint(paneRight, paneBottom);
		// create the bottom
		p.addPoint(tabPane.x, paneBottom);

		return p;
	}

	private Polygon createPolyTabOnBottom(Rectangle tabPane, Rectangle tab) {
		Polygon p = new Polygon();
		int tabRight = tab.x + tab.width;
		int paneRight = tabPane.x + tabPane.width;
		int paneBottom = tabPane.y + tabPane.height;
		int tabBottom = paneBottom + tab.height;

		// create the top
		p.addPoint(tabPane.x, tabPane.y);
		p.addPoint(paneRight, tabPane.y);
		// create the right-side
		p.addPoint(paneRight, paneBottom);

		// create the bottom
		p.addPoint(tabRight, paneBottom);
		p.addPoint(tabRight, tabBottom);
		p.addPoint(tab.x, tabBottom);

		// if the tab isn't all the way to the left, then create the path
		// until we reach the left-side
		if (tab.x != 0) {
			p.addPoint(tab.x, paneBottom);
			p.addPoint(tabPane.x, paneBottom);
		}

		return p;
	}

	private boolean isOuterRegion(Region region) {
		return region != Region.CENTER;
	}

	@Override
	public abstract void drawPreview(Graphics2D g, Polygon poly, Dockable dockable, Map dragInfo);
}
