/*
 * Created on Aug 11, 2005
 */
package org.flexdock.docking.defaults;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.flexdock.docking.Dockable;
import org.flexdock.docking.DockingPort;
import org.flexdock.docking.props.DockablePropertySet;
import org.flexdock.docking.props.PropertyChangeListenerFactory;

/**
 * @author Christopher Butler
 */
public class DockablePropertyChangeHandler implements PropertyChangeListener {
    public static final DockablePropertyChangeHandler DEFAULT_INSTANCE = new DockablePropertyChangeHandler(); 
    
    public void propertyChange(PropertyChangeEvent evt) {
        // System.out.println(evt.getSource() + ": " + evt.getOldValue() + " -> " + evt.getNewValue());
        if (evt.getPropertyName().equals(DockablePropertySet.TAB_ICON)
                || evt.getPropertyName().equals(DockablePropertySet.DESCRIPTION)) {
            if (evt.getSource() instanceof Dockable) {
                Dockable dockable = (Dockable) evt.getSource();
                DockingPort dockingPort = dockable.getDockingPort();
                if (dockingPort instanceof DefaultDockingPort) {
                    ((DefaultDockingPort) dockingPort).updateTab(dockable);
                }
            }
        }
    }
    
    public static class Factory extends PropertyChangeListenerFactory {
        public PropertyChangeListener getListener() {
            return DEFAULT_INSTANCE;
        }
    }
}
