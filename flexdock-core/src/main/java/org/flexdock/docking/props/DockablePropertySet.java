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
package org.flexdock.docking.props;

import org.flexdock.docking.DockingConstants;

import javax.swing.*;
import java.beans.PropertyChangeListener;

/**
 * @author Christopher Butler
 */
public interface DockablePropertySet {
	String DESCRIPTION = "Dockable.DESCRIPTION";
	String DOCKING_ENABLED = "Dockable.DOCKING_ENABLED";
	String MOUSE_MOTION_DRAG_BLOCK = "Dockable.MOUSE_MOTION_DRAG_BLOCK";
	String DRAG_THRESHOLD = "Dockable.DRAG_THRESHOLD";

	String REGION_SIZE_NORTH = "Dockable.REGION_SIZE_NORTH";
	String SIBLING_SIZE_NORTH = "Dockable.SIBLING_SIZE_NORTH";
	String TERRITORY_BLOCKED_NORTH = "Dockable.TERRITORY_BLOCKED_NORTH";

	String REGION_SIZE_SOUTH = "Dockable.REGION_SIZE_SOUTH";
	String SIBLING_SIZE_SOUTH = "Dockable.SIBLING_SIZE_SOUTH";
	String TERRITORY_BLOCKED_SOUTH = "Dockable.TERRITORY_BLOCKED_SOUTH";

	String REGION_SIZE_EAST = "Dockable.REGION_SIZE_EAST";
	String SIBLING_SIZE_EAST = "Dockable.SIBLING_SIZE_EAST";
	String TERRITORY_BLOCKED_EAST = "Dockable.TERRITORY_BLOCKED_EAST";

	String REGION_SIZE_WEST = "Dockable.REGION_SIZE_WEST";
	String SIBLING_SIZE_WEST = "Dockable.SIBLING_SIZE_WEST";
	String TERRITORY_BLOCKED_WEST = "Dockable.TERRITORY_BLOCKED_WEST";

	String TERRITORY_BLOCKED_CENTER = "Dockable.TERRITORY_BLOCKED_CENTER";
	String DOCKBAR_ICON = "Dockable.DOCKBAR_ICON";
	String TAB_ICON = "Dockable.TAB_ICON";
	String PREVIEW_SIZE = "Dockable.PREVIEW_SIZE";

	String ACTIVE = "Dockable.ACTIVE";


	String getDockableDesc();
	
	// TODO You can disable docking? But that's what this is all about
	Boolean isDockingEnabled();

	Boolean isMouseMotionListenersBlockedWhileDragging();

	Float getRegionInset(DockingConstants.Region region);

	Float getSiblingSize(DockingConstants.Region region);

	Boolean isTerritoryBlocked(DockingConstants.Region region);

	Float getDragThreshold();

	Icon getDockbarIcon();

	Icon getTabIcon();

	Float getPreviewSize();

	String getDockingId();

	Boolean isActive();


	void setDockableDesc(String desc);

	void setDockingEnabled(boolean enabled);

	void setMouseMotionListenersBlockedWhileDragging(boolean blocked);

	void setRegionInset(DockingConstants.Region region, float inset);

	void setSiblingSize(DockingConstants.Region region, float size);

	void setTerritoryBlocked(DockingConstants.Region region, boolean blocked);

	void setDragTheshold(float threshold);

	void setDockbarIcon(Icon icon);

	void setTabIcon(Icon icon);

	void setPreviewSize(float size);

	void setActive(boolean active);

	Object put(Object key, Object value);

	Object remove(Object key);

	void addPropertyChangeListener(PropertyChangeListener listener);

	void removePropertyChangeListener(PropertyChangeListener listener);
}
