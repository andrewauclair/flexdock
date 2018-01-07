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
package org.flexdock.perspective;

import org.flexdock.docking.Dockable;
import org.flexdock.docking.DockingManager;
import org.flexdock.docking.DockingPort;
import org.flexdock.docking.floating.frames.DockingFrame;
import org.flexdock.docking.state.DockingState;
import org.flexdock.docking.state.FloatManager;
import org.flexdock.docking.state.FloatingGroup;
import org.flexdock.docking.state.LayoutNode;
import org.flexdock.event.EventManager;
import org.flexdock.event.RegistrationEvent;
import org.flexdock.perspective.event.LayoutEvent;
import org.flexdock.perspective.event.LayoutListener;
import org.flexdock.util.DockingUtility;
import org.flexdock.util.RootWindow;
import org.flexdock.util.SwingUtility;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

/**
 * @author Christopher Butler
 */
public class Layout implements Cloneable, FloatManager, Serializable {

    private final HashMap<String, DockingState> dockingInfo;  // contains DockingState objects
    private final Hashtable<String, FloatingGroup> floatingGroups;
    private LayoutNode restorationLayout;

    private transient ArrayList<LayoutListener> layoutListeners;

    public Layout() {
        this(new HashMap<>(), new ArrayList<>(), new Hashtable<>());
    }

    private Layout(HashMap<String, DockingState> info, ArrayList<LayoutListener> listeners, Hashtable<String, FloatingGroup> floatGroups) {
        dockingInfo = info;
        layoutListeners = listeners;
        floatingGroups = floatGroups;
    }

    private ArrayList<LayoutListener> getLayoutListeners() {
        if (layoutListeners == null) {
            layoutListeners = new ArrayList<>();
        }
        return layoutListeners;
    }

    public void addListener(LayoutListener listener) {
        if (listener != null) {
            synchronized (getLayoutListeners()) {
                getLayoutListeners().add(listener);
            }
        }
    }

    public void removeListener(LayoutListener listener) {
        if (listener != null) {
            synchronized (getLayoutListeners()) {
                getLayoutListeners().remove(listener);
            }
        }
    }

    public LayoutListener[] getListeners() {
        return getLayoutListeners().toArray(new LayoutListener[0]);
    }

    public void add(Dockable dockable) {
        String key = dockable == null ? null : dockable.getPersistentId();
        add(key);
    }

    public void add(String dockableId) {
        if (dockableId == null) {
            return;
        }

        DockingState info = new DockingState(dockableId);
        synchronized (dockingInfo) {
            // return if we're already managing this dockable
            if (!dockingInfo.containsKey(dockableId)) {
                dockingInfo.put(dockableId, info);
            }
        }

        EventManager.dispatch(new RegistrationEvent(info, this, true));
    }

    public DockingState remove(String dockableId) {
        if (dockableId == null) {
            return null;
        }

        DockingState info;
        synchronized (dockingInfo) {
            info = dockingInfo.remove(dockableId);
        }
        // dispatch event notification if we actually removed something
        if (info != null) {
            EventManager.dispatch(new RegistrationEvent(info, this, false));
        }
        return info;
    }

    private boolean contains(Dockable dockable) {
        return dockable != null && contains(dockable.getPersistentId());
    }

    private boolean contains(String dockable) {
        return dockable != null && dockingInfo.containsKey(dockable);
    }

    public Dockable getDockable(String id) {
        if (dockingInfo.containsKey(id)) {
            return DockingManager.getDockable(id);
        }
        return null;
    }

    public Dockable[] getDockables() {
        ArrayList<Dockable> list = new ArrayList<>(dockingInfo.size());
        for (String dockingId : dockingInfo.keySet()) {
            Dockable d = DockingManager.getDockable(dockingId);
            if (d != null) {
                list.add(d);
            }
        }
        return list.toArray(new Dockable[0]);
    }

    public DockingState getDockingState(String dockableId) {
        return getDockingState(dockableId, false);
    }

    public DockingState getDockingState(Dockable dockable) {
        return getDockingState(dockable, false);
    }

    public DockingState getDockingState(Dockable dockable, boolean load) {
        if (dockable == null) {
            return null;
        }

        return getDockingState(dockable.getPersistentId(), load);
    }

    public DockingState getDockingState(String dockableId, boolean load) {
        if (dockableId == null) {
            return null;
        }

        if (load) {
            Dockable dockable = DockingManager.getDockable(dockableId);
            if (dockable != null) {
                isMaintained(dockable);
            }
        }
        Object obj = dockingInfo.get(dockableId);
        return (DockingState) obj;
    }

    public void setDockingState(String dockableId, DockingState dockingState) {
        if (dockableId == null || dockingState == null) {
            return;
        }
        this.dockingInfo.put(dockableId, dockingState);
    }

    public void apply(DockingPort dockingPort) {
        Component comp = (Component) dockingPort;
        if (comp == null || !isInitialized()) {
            //                if(comp==null || comp.getParent()==null || !isInitialized())
            return;
        }

        // clear out the existing components
        PerspectiveManager.clear(dockingPort);

        // restore the layout
        boolean listening = PerspectiveManager.isDockingStateListening();
        PerspectiveManager.setDockingStateListening(false);
        try {
            dockingPort.importLayout(restorationLayout);
        } finally {
            PerspectiveManager.setDockingStateListening(listening);
        }

        // not restore floating and minimized layouts
        Dockable[] dockables = getDockables();

        // if there is no active window into which to restore our minimized
        // dockables, then we'll have to defer restoration until a window appears.
        ArrayList<Dockable> deferredMinimizedDockables = new ArrayList<>();
        boolean deferMinimized = SwingUtility.getActiveWindow() == null;

        boolean restoreFloatOnLoad = PerspectiveManager.isRestoreFloatingOnLoad();
        for (Dockable dockable : dockables) {
            if (DockingUtility.isMinimized(dockable)) {
                if (deferMinimized) {
                    deferredMinimizedDockables.add(dockable);
                } else {
                    RestorationManager.getInstance().restore(dockable);
                }

            } else if (restoreFloatOnLoad && DockingUtility.isFloating(dockable)) {
                RestorationManager.getInstance().restore(dockable);
            }
        }

        // if necessary, defer minimized restoration until after a valid window
        // has been resolved
        restoreDeferredMinimizedDockables(deferredMinimizedDockables);

        // send notification
        LayoutEvent evt = new LayoutEvent(this, null, null, LayoutEvent.LAYOUT_APPLIED);
        EventManager.dispatch(evt);
    }

    private void restoreDeferredMinimizedDockables(final ArrayList deferred) {
        if (deferred == null || deferred.isEmpty()) {
            return;
        }

        EventQueue.invokeLater(() -> restoreMinimizedDockables(deferred));
    }


    private void restoreMinimizedDockables(ArrayList dockables) {
        if (SwingUtility.getActiveWindow() == null) {
            restoreDeferredMinimizedDockables(dockables);
            return;
        }

        for (Object dockable1 : dockables) {
            Dockable dockable = (Dockable) dockable1;
            RestorationManager.getInstance().restore(dockable);
        }
    }

    private boolean isMaintained(Dockable dockable) {
        if (dockable == null) {
            return false;
        }

        if (!contains(dockable)) {
            add(dockable);
        }
        return true;
    }

    public void hide(Dockable dockable) {
        if (!isMaintained(dockable)) {
            return;
        }

        boolean hidden = false;
        if (DockingManager.isDocked(dockable)) {
            hidden = DockingManager.undock(dockable);
        } else if (DockingUtility.isMinimized(dockable)) {
            hidden = DockingManager.getMinimizeManager().close(dockable);
        }

        if (hidden) {
            LayoutEvent evt = new LayoutEvent(this, null, dockable.getPersistentId(), LayoutEvent.DOCKABLE_HIDDEN);
            EventManager.dispatch(evt);
        }
    }

    public void show(Dockable dockable, DockingPort dockingPort) {
        if (!isMaintained(dockable) || DockingManager.isDocked(dockable)) {
            return;
        }
    }

    @Override
    public Object clone() {
        synchronized (this) {
            ArrayList listeners = (ArrayList) getLayoutListeners().clone();
            HashMap infoMap = (HashMap) dockingInfo.clone();
            for (String key : dockingInfo.keySet()) {
                DockingState info = getDockingState(key);
                infoMap.put(key, info.clone());
            }

            Hashtable floatTable = (Hashtable) floatingGroups.clone();
            for (String key : floatingGroups.keySet()) {
                FloatingGroup group = floatingGroups.get(key);
                floatTable.put(key, group.clone());
            }

            // note, we're using a shallow copy of the listener list.
            // it's okay that we share listener references, since we want the
            // cloned Layout to have the same listeners.
            Layout clone = new Layout(infoMap, listeners, floatTable);
            LayoutNode restoreNode = restorationLayout == null ? null : (LayoutNode) restorationLayout.clone();
            clone.restorationLayout = restoreNode;
            return clone;
        }

    }

    private DockingFrame getDockingFrame(Dockable dockable, Component frameOwner) {
        FloatingGroup group = getGroup(dockable);
        if (group == null) {
            group = new FloatingGroup(getFloatingGroup(dockable));
        }

        DockingFrame frame = group.getFrame();
        if (frame == null) {
            frame = new DockingFrame(group.getName(), true);
            group.setFrame(frame);
            floatingGroups.put(group.getName(), group);
        }
        return frame;
    }

    @Override
    public DockingFrame floatDockable(Dockable dockable, Component frameOwner, Rectangle screenBounds) {
        if (dockable == null || screenBounds == null) {
            return null;
        }

        // create the frame
        DockingFrame frame = getDockingFrame(dockable, frameOwner);
        if (screenBounds != null) {
            frame.setBounds(screenBounds);
        }

        // undock the current Dockable instance from it's current parent container
        DockingManager.undock(dockable);

        // add to the floating frame
        frame.addDockable(dockable);

        // display and return
        if (!frame.isVisible()) {
            frame.setVisible(true);
        }
        return frame;
    }

    @Override
    public DockingFrame floatDockable(Dockable dockable, Component frameOwner) {
        FloatingGroup group = getGroup(dockable);
        Rectangle bounds = group == null ? null : group.getBounds();
        if (bounds == null) {
            if (dockable.getComponent().isValid()) {
                bounds = dockable.getComponent().getBounds();
            } else {
                bounds = new Rectangle(0, 0, 200, 200);
            }

            Rectangle ownerBounds = frameOwner instanceof DockingFrame ?
                    ((Window) frameOwner).getOwner().getBounds() :
                    RootWindow.getRootContainer(frameOwner).getRootContainer().getBounds();

            int x = (ownerBounds.x + ownerBounds.width / 2) - bounds.width / 2;
            int y = (ownerBounds.y + ownerBounds.height / 2) - bounds.height / 2;
            bounds.setLocation(x, y);
        }

        return floatDockable(dockable, frameOwner, bounds);
    }

    @Override
    public FloatingGroup getGroup(Dockable dockable) {
        if (dockable == null) {
            return null;
        }

        String groupId = getFloatingGroup(dockable);
        return getGroup(groupId);
    }

    public String[] getFloatingGroupIds() {
        return this.floatingGroups.keySet().toArray(new String[0]);
    }

    @Override
    public FloatingGroup getGroup(String groupId) {
        return groupId == null ? null : floatingGroups.get(groupId);
    }

    public void addFloatingGroup(FloatingGroup floatingGroup) {
        if (floatingGroup == null) {
            return;
        }
        floatingGroups.put(floatingGroup.getName(), floatingGroup);
    }

    @Override
    public void addToGroup(Dockable dockable, String groupId) {
        // floating groups are mutually exclusive
        removeFromGroup(dockable);

        FloatingGroup group = getGroup(groupId);
        if (dockable != null && group != null) {
            group.addDockable(dockable.getPersistentId());
            setFloatingGroup(dockable, group.getName());
        }
    }

    @Override
    public void removeFromGroup(Dockable dockable) {
        FloatingGroup group = getGroup(dockable);
        if (dockable != null) {
            if (group != null) {
                group.removeDockable(dockable.getPersistentId());
            }
            setFloatingGroup(dockable, null);
        }

        // if the group is empty, dispose of it so we don't have
        // any memory leaks
        if (group != null && group.getDockableCount() == 0) {
            floatingGroups.remove(group.getName());
            group.destroy();
        }
    }

    private String getFloatingGroup(Dockable dockable) {
        DockingState info = getDockingState(dockable, false);
        return info.getFloatingGroup();
    }

    private void setFloatingGroup(Dockable dockable, String group) {
        DockingState info = getDockingState(dockable, false);
        info.setFloatingGroup(group);
    }

    public boolean isInitialized() {
        return restorationLayout != null;
    }

    public LayoutNode getRestorationLayout() {
        return restorationLayout;
    }

    public void setRestorationLayout(LayoutNode restorationLayout) {
        this.restorationLayout = restorationLayout;
    }

    void update(LayoutSequence sequence) {
        List states = sequence.getDockingStates();

        synchronized (dockingInfo) {
            for (Object state : states) {
                DockingState info = (DockingState) state;
                dockingInfo.put(info.getDockableId(), info);
            }
        }
    }
}
