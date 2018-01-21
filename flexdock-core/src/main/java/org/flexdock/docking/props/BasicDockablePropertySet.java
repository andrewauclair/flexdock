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

import org.flexdock.docking.Dockable;
import org.flexdock.util.Utilities;

import javax.swing.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Hashtable;
import java.util.Map;

import static java.lang.Boolean.getBoolean;
import static org.flexdock.docking.DockingConstants.Region;

/**
 * @author Christopher Butler
 */
@SuppressWarnings(value = {"serial"})
public class BasicDockablePropertySet extends Hashtable<Object, Object> implements DockablePropertySet {
    private String dockingId;
    private PropertyChangeSupport changeSupport;

	static String getRegionInsetKey(Region region) {
		switch (region) {
			case NORTH:
				return REGION_SIZE_NORTH;
			case SOUTH:
				return REGION_SIZE_SOUTH;
			case EAST:
				return REGION_SIZE_EAST;
			case WEST:
				return REGION_SIZE_WEST;
		}
        return null;
    }

    static String getSiblingSizeKey(String region) {
		if (Region.NORTH.toString().equals(region)) {
            return SIBLING_SIZE_NORTH;
        }
		if (Region.SOUTH.toString().equals(region)) {
            return SIBLING_SIZE_SOUTH;
        }
		if (Region.EAST.toString().equals(region)) {
            return SIBLING_SIZE_EAST;
        }
		if (Region.WEST.toString().equals(region)) {
            return SIBLING_SIZE_WEST;
        }
        return null;
    }

	static String getTerritoryBlockedKey(Region region) {
		switch (region) {
			case NORTH:
				return TERRITORY_BLOCKED_NORTH;
			case SOUTH:
				return TERRITORY_BLOCKED_SOUTH;
			case EAST:
				return TERRITORY_BLOCKED_EAST;
			case WEST:
				return TERRITORY_BLOCKED_WEST;
			case CENTER:
				return TERRITORY_BLOCKED_CENTER;
		}
        return null;
    }

    public BasicDockablePropertySet(Dockable dockable) {
        super();
        init(dockable);
    }

    BasicDockablePropertySet(int initialCapacity, Dockable dockable) {
        super(initialCapacity);
        init(dockable);
    }

    BasicDockablePropertySet(int initialCapacity, float loadFactor, Dockable dockable) {
        super(initialCapacity, loadFactor);
        init(dockable);
    }

    BasicDockablePropertySet(Map t, Dockable dockable) {
        super(t);
        init(dockable);
    }

    private void init(Dockable dockable) {
        this.dockingId = dockable == null ? null : dockable.getPersistentId();
        Object changeSrc = dockable == null ? this : dockable;
        changeSupport = new PropertyChangeSupport(changeSrc);
    }


    @Override
    public Icon getDockbarIcon() {
        return (Icon) get(DOCKBAR_ICON);
    }

    @Override
    public Icon getTabIcon() {
        return (Icon) get(TAB_ICON);
    }

    @Override
    public String getDockableDesc() {
        return (String) get(DESCRIPTION);
    }

    @Override
    public Boolean isDockingEnabled() {
        return (Boolean) get(DOCKING_ENABLED);
    }

    @Override
    public Boolean isActive() {
        return (Boolean) get(ACTIVE);
    }

    @Override
    public Boolean isMouseMotionListenersBlockedWhileDragging() {
        return getBoolean(MOUSE_MOTION_DRAG_BLOCK);
    }


    @Override
    public Float getRegionInset(String region) {
		String key = getRegionInsetKey(Region.valueOf(region));
        return key == null ? null : (Float) get(key);
    }

    @Override
    public Float getSiblingSize(String region) {
        String key = getSiblingSizeKey(region);
        return key == null ? null : (Float) get(key);
    }

    @Override
    public Boolean isTerritoryBlocked(String region) {
		String key = getTerritoryBlockedKey(Region.valueOf(region));
        return key == null ? null : (Boolean) get(key);
    }

    @Override
    public Float getDragThreshold() {
        return (Float) get(DRAG_THRESHOLD);
    }

    @Override
    public Float getPreviewSize() {
        return (Float) get(PREVIEW_SIZE);
    }


    @Override
    public void setDockbarIcon(Icon icon) {
        Icon oldValue = getDockbarIcon();
        put(DOCKBAR_ICON, icon);
        firePropertyChange(DOCKBAR_ICON, oldValue, icon);
    }

    @Override
    public void setTabIcon(Icon icon) {
        Icon oldValue = getTabIcon();
        put(TAB_ICON, icon);
        firePropertyChange(TAB_ICON, oldValue, icon);
    }

    @Override
    public void setDockableDesc(String dockableDesc) {
        String oldValue = getDockableDesc();
        put(DESCRIPTION, dockableDesc);
        firePropertyChange(DESCRIPTION, oldValue, dockableDesc);
    }

    @Override
    public void setDockingEnabled(boolean enabled) {
        put(DOCKING_ENABLED, enabled);
    }

    @Override
    public void setActive(boolean active) {
        Boolean oldValue = isActive();
        if (oldValue == null) {
            oldValue = Boolean.FALSE;
        }

        put(ACTIVE, active);
        firePropertyChange(ACTIVE, oldValue.booleanValue(), active);
    }

    @Override
    public void setMouseMotionListenersBlockedWhileDragging(boolean blocked) {
        put(MOUSE_MOTION_DRAG_BLOCK, blocked);
    }

    @Override
    public void setRegionInset(String region, float inset) {
		String key = getRegionInsetKey(Region.valueOf(region));
        if (key != null) {
            Float f = inset;
            put(key, f);
        }
    }

    @Override
    public void setSiblingSize(String region, float size) {
        String key = getSiblingSizeKey(region);
        if (key != null) {
            Float f = size;
            put(key, f);
        }
    }

    @Override
    public void setTerritoryBlocked(String region, boolean blocked) {
		String key = getTerritoryBlockedKey(Region.valueOf(region));
        if (key != null) {
			put(key, blocked ? Boolean.TRUE : Boolean.FALSE);
        }
    }


    @Override
    public void setDragTheshold(float threshold) {
		put(DRAG_THRESHOLD, Math.max(threshold, 0));
    }

    @Override
	public void setPreviewSize(float size) {
		put(PREVIEW_SIZE, Math.min(Math.max(size, 0.0f), 1.0f));
    }

    /**
     * @return Returns the dockingId.
     */
    @Override
    public String getDockingId() {
        return dockingId;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }

    private void firePropertyChange(String property, Object oldValue, Object newValue) {
        if (Utilities.isChanged(oldValue, newValue)) {
            changeSupport.firePropertyChange(property, oldValue, newValue);
        }
    }

    protected void firePropertyChange(String property, int oldValue, int newValue) {
        if (oldValue != newValue) {
            changeSupport.firePropertyChange(property, oldValue, newValue);
        }
    }

    private void firePropertyChange(String property, boolean oldValue, boolean newValue) {
        if (oldValue != newValue) {
            changeSupport.firePropertyChange(property, oldValue, newValue);
        }
    }
}
