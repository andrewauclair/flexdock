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
package org.flexdock.docking.event.hierarchy;

import org.flexdock.docking.Dockable;
import org.flexdock.docking.DockingManager;
import org.flexdock.docking.DockingPort;
import org.flexdock.util.RootWindow;

import java.applet.Applet;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * @author Christopher Butler
 * @author Karl Schaefer
 */
public class DockingPortTracker implements HierarchyListener {
	private static final DockingPortTracker SINGLETON = new DockingPortTracker();
	private static final WeakHashMap<RootWindow, RootDockingPortInfo> TRACKERS_BY_WINDOW = new WeakHashMap<>();
	private static final WeakHashMap<DockingPort, Object> DOCKING_PORTS = new WeakHashMap<>();

	public static HierarchyListener getInstance() {
		return SINGLETON;
	}

	public static void remove(Component c) {
		RootWindow window = RootWindow.getRootContainer(c);
		if (window != null) {
			synchronized (TRACKERS_BY_WINDOW) {
				TRACKERS_BY_WINDOW.remove(window);
			}
		}
	}

	public static RootDockingPortInfo getRootDockingPortInfo(Component c) {
		RootWindow window = RootWindow.getRootContainer(c);
		return getRootDockingPortInfo(window);
	}

	private static RootDockingPortInfo getRootDockingPortInfo(RootWindow window) {
		if (window == null) {
			return null;
		}

		RootDockingPortInfo info = TRACKERS_BY_WINDOW.get(window);
		if (info == null) {
			synchronized (TRACKERS_BY_WINDOW) {
				info = new RootDockingPortInfo(window);
				TRACKERS_BY_WINDOW.put(window, info);
			}
		}
		return info;
	}

	public static DockingPort findById(String portId) {
		if (portId == null) {
			return null;
		}

		synchronized (TRACKERS_BY_WINDOW) {
			for (RootDockingPortInfo info : TRACKERS_BY_WINDOW.values()) {
				DockingPort port = info.getPort(portId);
				if (port != null) {
					return port;
				}
			}
		}
		return null;
	}

	private static RootDockingPortInfo findInfoByPort(DockingPort port) {
		if (port == null) {
			return null;
		}

		synchronized (TRACKERS_BY_WINDOW) {
			for (RootDockingPortInfo info : TRACKERS_BY_WINDOW.values()) {
				if (info.contains(port)) {
					return info;
				}
			}
		}
		return null;
	}

	/**
	 * Returns the {@code DockingPort} for {@code comp}. If {@code comp} is
	 * {@code null}, then this method returns {@code null}.
	 *
	 * @param comp the component for which to find the root docking port.
	 * @return the eldest docking port for {@code comp}, or {@code null} if
	 * {@code comp} is {@code null} or has no {@code DockingPort}
	 * ancestor.
	 */
	public static DockingPort findByWindow(Component comp) {
		Component c = comp;
		DockingPort port = null;

		while (c != null) {
			if (c instanceof DockingPort) {
				port = (DockingPort) c;
			}

			c = c.getParent();
		}

		if (port == null) {
			port = findByWindow(RootWindow.getRootContainer(comp));
		}

		return port;
	}


	private static DockingPort findByWindow(RootWindow window) {
		RootDockingPortInfo info = getRootDockingPortInfo(window);
		if (info == null) {
			return null;
		}

		return info.getPort(0);
	}


	public static void updateIndex(DockingPort port) {
		if (port == null) {
			return;
		}

		synchronized (DOCKING_PORTS) {
			DOCKING_PORTS.put(port, new Object());
		}

		RootDockingPortInfo info = findInfoByPort(port);
		if (info != null) {
			info.remove(port);
			info.add(port);
		}
	}


	private boolean isParentChange(HierarchyEvent evt) {
		return evt.getID() == HierarchyEvent.HIERARCHY_CHANGED && evt.getChangeFlags() == HierarchyEvent.PARENT_CHANGED;
	}

	private boolean isRemoval(HierarchyEvent evt) {
		return evt.getChanged().getParent() == null;
	}


	@Override
	public void hierarchyChanged(HierarchyEvent evt) {
		// only work with DockingPorts
		if (!(evt.getSource() instanceof DockingPort)) {
			return;
		}

		// we don't want to work with sub-ports
		DockingPort port = (DockingPort) evt.getSource();
		if (!port.isRoot()) {
			return;
		}

		// only work with parent-change events
		if (!isParentChange(evt)) {
			return;
		}

		// root-ports are tracked by window.  if we can't find a parent window, then we
		// can track the dockingport.
		Container changedParent = evt.getChangedParent();
		RootWindow window = RootWindow.getRootContainer(changedParent);
		if (window == null) {
			return;
		}

		boolean removal = isRemoval(evt);
		if (removal) {
			dockingPortRemoved(window, port);
		}
		else {
			dockingPortAdded(window, port);
		}
	}

	private void dockingPortAdded(RootWindow window, DockingPort port) {
		RootDockingPortInfo info = getRootDockingPortInfo(window);
		if (info != null) {
			info.add(port);
		}
	}

	private void dockingPortRemoved(RootWindow window, DockingPort port) {
		RootDockingPortInfo info = getRootDockingPortInfo(window);
		if (info != null) {
			info.remove(port);
		}
	}

	public static Set<RootWindow> getDockingWindows() {
		synchronized (TRACKERS_BY_WINDOW) {
			return new HashSet<>(TRACKERS_BY_WINDOW.keySet());
		}
	}

	private static Set<DockingPort> getDockingPorts() {
		Set<DockingPort> globalSet;
		synchronized (DOCKING_PORTS) {
			globalSet = new HashSet<>(DOCKING_PORTS.keySet());
		}
		return globalSet;
	}

	public static Set<DockingPort> getRootDockingPorts() {
		HashSet<DockingPort> rootSet = new HashSet<>();
		Set<DockingPort> globalSet = getDockingPorts();

		for (Object aGlobalSet : globalSet) {
			DockingPort port = (DockingPort) aGlobalSet;
			if (port.isRoot()) {
				rootSet.add(port);
			}
		}
		return rootSet;
	}

	public static DockingPort getRootDockingPort(Dockable dockable) {
		if (dockable == null || !DockingManager.isDocked(dockable)) {
			return null;
		}

		DockingPort port = dockable.getDockingPort();
		Container parent = ((Component) port).getParent();
		while (!isWindowRoot(parent)) {
			if (parent instanceof DockingPort) {
				port = (DockingPort) parent;
			}
			parent = parent.getParent();
		}

		return port;
	}

	private static boolean isWindowRoot(Component comp) {
		return comp instanceof Window || comp instanceof Applet;
	}
}
