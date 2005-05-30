/*
 * Created on May 17, 2005
 */
package org.flexdock.perspective;

import java.awt.Component;
import java.awt.Rectangle;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

import org.flexdock.docking.Dockable;
import org.flexdock.docking.DockingManager;
import org.flexdock.docking.DockingPort;
import org.flexdock.docking.floating.frames.DockingFrame;
import org.flexdock.docking.state.DockingState;
import org.flexdock.docking.state.DockingStateComparator;
import org.flexdock.docking.state.FloatManager;
import org.flexdock.docking.state.FloatingGroup;
import org.flexdock.docking.state.LayoutNode;
import org.flexdock.event.EventDispatcher;
import org.flexdock.perspective.event.LayoutEvent;
import org.flexdock.perspective.event.LayoutListener;
import org.flexdock.perspective.event.RegistrationEvent;
import org.flexdock.util.DockingUtility;
import org.flexdock.util.RootWindow;

/**
 * @author Christopher Butler
 */
public class Layout implements Cloneable, FloatManager, Serializable {
	private HashMap dockingInfo;
	private ArrayList layoutListeners;
	private Hashtable floatingGroups;
	private LayoutNode restorationLayout;
	
	public Layout() {
		this(new HashMap(), new ArrayList(), new Hashtable());	
	}
	
	private Layout(HashMap info, ArrayList listeners, Hashtable floatGroups) {
		dockingInfo = info;
		layoutListeners = listeners;
		floatingGroups = floatGroups;
	}
	
	public void addListener(LayoutListener listener) {
		if(listener!=null) {
			synchronized(layoutListeners) {
				layoutListeners.add(listener);
			}
		}
	}
	
	public void removeListener(LayoutListener listener) {
		if(listener!=null) {
			synchronized(layoutListeners) {
				layoutListeners.remove(listener);
			}
		}
	}
	
	public LayoutListener[] getListeners() {
		return (LayoutListener[])layoutListeners.toArray(new LayoutListener[0]);
	}
	
	
	
	
	
	
	public void add(Dockable dockable) {
		String key = dockable==null? null: dockable.getPersistentId();
		add(key);
	}
	
	public void add(String dockableId) {
		if(dockableId==null)
			return;
		
		DockingState info = null;
		synchronized(dockingInfo) {
			info = (DockingState)dockingInfo.get(dockableId);
			// return if we're already managing this dockable
			if(info!=null)
				return;
			
			// create and add dockingstateinfo here
			info = new DockingState(dockableId, dockingInfo.size());
			dockingInfo.put(dockableId, info);
		}

		EventDispatcher.dispatch(new RegistrationEvent(info, this, true));
	}
	
	public DockingState remove(String dockableId) {
		if(dockableId==null)
			return null;
		
		DockingState info = null;
		synchronized(dockingInfo) {
			info = (DockingState)dockingInfo.remove(dockableId);
		}
		// dispatch event notification if we actually removed something
		if(info!=null)
			EventDispatcher.dispatch(new RegistrationEvent(info, this, false));
		return info;
	}
	
	public boolean contains(Dockable dockable) {
		return dockable==null? false: contains(dockable.getPersistentId());
	}
	
	public boolean contains(String dockable) {
		return dockable==null? false: dockingInfo.containsKey(dockable);
	}
	
	public Dockable getDockable(String id) {
		if(dockingInfo.containsKey(id))
			return DockingManager.getDockable(id);
		return null;
	}
		
	public Dockable[] getDockables() {
		ArrayList list = new ArrayList(dockingInfo.size());
		for(Iterator it=dockingInfo.keySet().iterator(); it.hasNext();) {
			String dockingId = (String)it.next();
			Dockable d = DockingManager.getDockable(dockingId);
			if(d!=null)
				list.add(d);
		}
		return (Dockable[])list.toArray(new Dockable[0]);
	}
	
	
	
	
	
	
	
	
	
	
	public DockingState getDockingState(String dockableId) {
		return getDockingState(dockableId, false);
	}
	
	public DockingState getDockingState(Dockable dockable) {
		return getDockingState(dockable, false);
	}

	public DockingState getDockingState(Dockable dockable, boolean load) {
		if(dockable==null)
			return null;

		return getDockingState(dockable.getPersistentId(), load);
	}
	
	public DockingState getDockingState(String dockableId, boolean load) {
		if(dockableId==null)
			return null;
		
		if(load) {
			Dockable dockable = DockingManager.getDockable(dockableId);
			if(dockable!=null) {
				isMaintained(dockable);
			}
		}
		return (DockingState)dockingInfo.get(dockableId);
	}

	
	private DockingState[] getApplySequence() {
		ArrayList list = new ArrayList(dockingInfo.values());
		Collections.sort(list, new DockingStateComparator());
		return (DockingState[])list.toArray(new DockingState[0]);
	}
	
	public void apply(DockingPort dockingPort) {
		Component comp = (Component)dockingPort;
		if(comp==null || comp.getParent()==null || !isInitialized())
			return;
		
		// clear out the existing components
		PerspectiveManager.clear(dockingPort);
		
		// restore the layout
		boolean listening = PerspectiveManager.isDockingStateListening();
		PerspectiveManager.setDockingStateListening(false);
		dockingPort.importLayout(restorationLayout);
		PerspectiveManager.setDockingStateListening(listening);
		
		// send notification
		LayoutEvent evt = new LayoutEvent(this, null, null, LayoutEvent.LAYOUT_APPLIED);
		EventDispatcher.dispatch(evt);
	}

	
	private boolean isMaintained(Dockable dockable) {
		if(dockable==null)
			return false;
		
		if(!contains(dockable))
			add(dockable);
		return true;
	}

	public void hide(Dockable dockable) {
		if(!isMaintained(dockable))
			return;
		
		boolean hidden = false;
		if(DockingManager.isDocked((Dockable)dockable)) {
			hidden = DockingManager.undock(dockable);
		} else if (DockingUtility.isMinimized(dockable)) {
			hidden = DockingManager.getMinimizeManager().close(dockable);
		}
		
		if(hidden) {
			LayoutEvent evt = new LayoutEvent(this, null, dockable.getPersistentId(), LayoutEvent.DOCKABLE_HIDDEN);
			EventDispatcher.dispatch(evt);
		}
	}
	
	public void show(Dockable dockable, DockingPort dockingPort) {
		if(!isMaintained(dockable) || DockingManager.isDocked(dockable))
			return;
	}
	
	public Object clone() {
		synchronized(this) {
			ArrayList listeners = (ArrayList)layoutListeners.clone();
			HashMap infoMap = (HashMap)dockingInfo.clone();
			for(Iterator it=dockingInfo.keySet().iterator(); it.hasNext();) {
				String key = (String)it.next();
				DockingState info = getDockingState(key);
				infoMap.put(key, info.clone());
			}
			
			Hashtable floatTable = (Hashtable)floatingGroups.clone();
			for(Iterator it=floatingGroups.keySet().iterator(); it.hasNext();) {
				Object key = it.next();
				FloatingGroup group = (FloatingGroup)floatingGroups.get(key);
				infoMap.put(key, group.clone());
			}
			
			// note, we're using a shallow copy of the listener list.
			// it's okay that we share listener references, since we want the
			// cloned Layout to have the same listeners.
			Layout clone = new Layout(infoMap, listeners, floatTable);
			LayoutNode restoreNode = restorationLayout==null? null: (LayoutNode)restorationLayout.clone();
			clone.restorationLayout = restoreNode;
			return clone;
		}

	}

	
	
	
	
	
	
	
	
	private DockingFrame getDockingFrame(Dockable dockable, Component frameOwner) {
		FloatingGroup group = getGroup(dockable);
		if(group==null)
			group = new FloatingGroup(dockable.getDockingProperties().getFloatingGroup());
		
		DockingFrame frame = group.getFrame();
		if(frame==null) {
			frame = DockingFrame.create(frameOwner, group.getName());
			group.setFrame(frame);
			floatingGroups.put(group.getName(), group);
		}
		return frame;
	}
	
	public DockingFrame floatDockable(Dockable dockable, Component frameOwner, Rectangle screenBounds) {
		if(dockable==null || screenBounds==null)
			return null;
		
		// create the frame
		DockingFrame frame = getDockingFrame(dockable, frameOwner);
		if(screenBounds!=null)
			frame.setBounds(screenBounds);

		// undock the current Dockable instance from it's current parent container
		DockingManager.undock(dockable);

		// add to the floating frame
		frame.addDockable(dockable);
		
		// display and return
		if(!frame.isVisible())
			frame.setVisible(true);
		return frame;
	}
	
	public DockingFrame floatDockable(Dockable dockable, Component frameOwner) {
		FloatingGroup group = getGroup(dockable);
		Rectangle bounds = group==null? null: group.getBounds();
		if(bounds==null) {
			if(dockable.getDockable().isValid()) {
				bounds = dockable.getDockable().getBounds();
			}
			else
				bounds = new Rectangle(0, 0, 200, 200);
			
			Rectangle ownerBounds = frameOwner instanceof DockingFrame? 
			((DockingFrame)frameOwner).getOwner().getBounds():
			RootWindow.getRootContainer(frameOwner).getRootContainer().getBounds();

			int x = (ownerBounds.x + ownerBounds.width/2) - bounds.width/2;
			int y = (ownerBounds.y + ownerBounds.height/2) - bounds.height/2;
			bounds.setLocation(x, y);
		}
		
		return floatDockable(dockable, frameOwner, bounds);
	}
	
	public FloatingGroup getGroup(Dockable dockable) {
		if(dockable==null)
			return null;
		
		String groupId = dockable.getDockingProperties().getFloatingGroup();
		return getGroup(groupId);
	}
	
	public FloatingGroup getGroup(String groupId) {
		return groupId==null? null: (FloatingGroup)floatingGroups.get(groupId);
	}
	
	public void addToGroup(Dockable dockable, String groupId) {
		// floating groups are mutually exclusive
		removeFromGroup(dockable);

		FloatingGroup group = getGroup(groupId);
		if(dockable!=null && group!=null) {
			group.addDockable(dockable.getPersistentId());
			dockable.getDockingProperties().setFloatingGroup(groupId);
		}
	}
	
	public void removeFromGroup(Dockable dockable) {
		FloatingGroup group = getGroup(dockable);
		if(dockable!=null) {
			if(group!=null)
				group.removeDockable(dockable.getPersistentId());
			dockable.getDockingProperties().setFloatingGroup(null);
		}
		
		// if the group is empty, dispose of it so we don't have 
		// any memory leaks
		if(group!=null && group.getDockableCount()==0) {
			floatingGroups.remove(group.getName());
			group.destroy();
		}
	}
	
	public boolean isInitialized() {
		return restorationLayout!=null;
	}
	
	
	public LayoutNode getRestorationLayout() {
		return restorationLayout;
	}
	public void setRestorationLayout(LayoutNode restorationLayout) {
		this.restorationLayout = restorationLayout;
	}
}