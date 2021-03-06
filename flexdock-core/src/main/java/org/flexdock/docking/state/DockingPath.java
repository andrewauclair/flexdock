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
package org.flexdock.docking.state;

import org.flexdock.docking.Dockable;
import org.flexdock.docking.DockingConstants;
import org.flexdock.docking.DockingManager;
import org.flexdock.docking.DockingPort;
import org.flexdock.docking.defaults.DockingSplitPane;
import org.flexdock.docking.state.tree.SplitNode;
import org.flexdock.util.DockingUtility;
import org.flexdock.util.SwingUtility;

import javax.swing.*;
import java.awt.*;
import java.io.Serializable;
import java.util.*;
import java.util.List;

/**
 * @author Christopher Butler
 */
@SuppressWarnings(value = {"serial"})
public class DockingPath implements Cloneable, Serializable {
	
	private static final String RESTORE_PATH_KEY = "DockingPath.RESTORE_PATH_KEY";
	
	private transient String stringForm;
	private String rootPortId;
	private final ArrayList<SplitNode> nodes = new ArrayList<>(); // contains SplitNode objects
	private String siblingId;
	private boolean tabbed;

	public DockingPath() {
	}

	public static DockingPath create(String dockableId) {
		Dockable dockable = findDockable(dockableId);
		return create(dockable);
	}
	
	public static DockingPath create(Dockable dockable) {
		if (dockable == null || !isDocked(dockable)) {
			return null;
		}
		
		DockingPath path = new DockingPath(dockable);
		Component comp = dockable.getComponent();
		
		Container parent = comp.getParent();
		while (!isDockingRoot(parent)) {
			if (parent instanceof DockingPort) {
				SplitNode node = createNode((DockingPort) parent);
				path.addNode(node);
			}
			parent = parent.getParent();
		}
		if (isDockingRoot(parent)) {
			path.setRootPortId(((DockingPort) parent).getPersistentId());
		}
		
		path.initialize();
		return path;
	}
	
	public static SplitNode createNode(Dockable dockable) {
		if (dockable == null) {
			return null;
		}
		
		Container parent = dockable.getComponent().getParent();
		return parent instanceof DockingPort ? createNode((DockingPort) parent) : null;
	}
	
	private static SplitNode createNode(DockingPort port) {
		if (port == null) {
			return null;
		}
		
		Component c = ((Component) port).getParent();
		JSplitPane split = c instanceof JSplitPane ? (JSplitPane) c : null;
		if (split == null) {
			return null;
		}
		
		return createNode(port, split);
	}
	
	private static SplitNode createNode(DockingPort port, JSplitPane split) {
		int orientation = split.getOrientation();
		boolean topLeft = split.getLeftComponent() == port;

		int region;
		String siblingId;
		if (topLeft) {
			region = orientation == JSplitPane.VERTICAL_SPLIT ? SwingConstants.TOP : SwingConstants.LEFT;
			siblingId = getSiblingId(split.getRightComponent());
		}
		else {
			region = orientation == JSplitPane.VERTICAL_SPLIT ? SwingConstants.BOTTOM : SwingConstants.RIGHT;
			siblingId = getSiblingId(split.getLeftComponent());
		}
		
		int size = orientation == JSplitPane.VERTICAL_SPLIT ? split.getHeight() : split.getWidth();
		int divLoc = split.getDividerLocation();

		float percentage;
		if (split instanceof DockingSplitPane && ((DockingSplitPane) split).getPercent() != -1) {
			percentage = (float) ((DockingSplitPane) split).getPercent();
		}
		else {
			percentage = divLoc / (float) size;
		}
		
		return new SplitNode(orientation, region, percentage, siblingId);
	}

	private static String getSiblingId(Component sibling) {
		Component component = sibling;
		if (component instanceof DockingPort) {
			component = ((DockingPort) component).getDockedComponent();
		}

		Dockable dockable = findDockable(component);
		return dockable == null ? null : dockable.getPersistentId();
	}
	
	
	private static boolean isDockingRoot(Container c) {
		return c instanceof DockingPort && ((DockingPort) c).isRoot();
	}
	
	public static DockingPath getRestorePath(Dockable dockable) {
		Object obj = dockable == null ? null : dockable.getClientProperty(RESTORE_PATH_KEY);
		return obj instanceof DockingPath ? (DockingPath) obj : null;
	}
	
	public static DockingPath updateRestorePath(Dockable dockable, DockingPath restorePath) {
		if (dockable == null || restorePath == null) {
			return null;
		}
		dockable.putClientProperty(RESTORE_PATH_KEY, restorePath);
		return restorePath;
	}
	
	private DockingPath(Dockable dockable) {
		siblingId = findSiblingId(dockable);
		tabbed = dockable.getComponent().getParent() instanceof JTabbedPane;
	}
	
	public boolean isTabbed() {
		return this.tabbed;
	}
	
	public void setTabbed(boolean isTabbed) {
		this.tabbed = isTabbed;
	}
	
	public String getSiblingId() {
		return this.siblingId;
	}
	
	public void setSiblingId(String siblingId) {
		this.siblingId = siblingId;
	}
	
	private DockingPath(String parent, boolean tabs, ArrayList nodeList) {
		siblingId = parent;
		tabbed = tabs;
	}
	
	public List getNodes() {
		return nodes;
	}

	public String getRootPortId() {
		return this.rootPortId;
	}
	
	public void setRootPortId(String portId) {
		rootPortId = portId;
	}
	
	private void addNode(SplitNode node) {
		nodes.add(node);
	}
	
	private void initialize() {
		Collections.reverse(nodes);
	}
	
	private String findSiblingId(Dockable dockable) {
		Component comp = dockable.getComponent();
		JSplitPane split = comp.getParent() instanceof JSplitPane ? (JSplitPane) comp.getParent() : null;
		if (split == null) {
			return null;
		}
		
		Component sibling = split.getLeftComponent();
		if (comp == sibling) {
			sibling = split.getRightComponent();
		}
		
		Dockable d = findDockable(sibling);
		return d == null ? null : d.getPersistentId();
	}
	
	@Override
	public String toString() {
		if (stringForm == null) {
			StringBuilder sb = new StringBuilder("/RootPort[id=").append(rootPortId).append(']');
			for (SplitNode node : nodes) {
				sb.append('/').append(node.toString());
			}
			sb.append("/Dockable");
			stringForm = sb.toString();
		}
		return stringForm;
	}
	
	public boolean restore(String dockable) {
		return restore(DockingManager.getDockable(dockable));
	}
	
	private DockingPort getRootDockingPort() {
		DockingPort port = DockingManager.getDockingPort(rootPortId);
		if (port != null) {
			return port;
		}
		
		Window activeWindow = SwingUtility.getActiveWindow();
		return DockingManager.getRootDockingPort(activeWindow);
	}
	
	public boolean restore(Dockable dockable) {
		if (dockable == null || isDocked(dockable)) {
			return false;
		}
		
		DockingPort rootPort = getRootDockingPort();
		DockingConstants.Region region = DockingConstants.Region.CENTER;
		if (nodes.isEmpty()) {
			return dockFullPath(dockable, rootPort, region);
		}
		
		DockingPort port = rootPort;
		for (SplitNode node : nodes) {
			Component comp = port.getDockedComponent();
			region = getRegion(node, comp);

			JSplitPane splitPane = comp instanceof JSplitPane ? (JSplitPane) comp : null;
			// path was broken.  we have no SplitPane, or the SplitPane doesn't
			// match the orientation of the current node, meaning the path was
			// altered at this point.
			if (splitPane == null || splitPane.getOrientation() != node.getOrientation()) {
				return dockBrokenPath(dockable, port, region, node);
			}

			// assume there is a transient sub-dockingPort in the split pane
			comp = node.getRegion() == SwingConstants.LEFT || node.getRegion() == SwingConstants.TOP ? splitPane.getLeftComponent() : splitPane.getRightComponent();
			port = (DockingPort) comp;

			// move on to the next node
		}
		
		return dockFullPath(dockable, port, region);
	}


	private boolean dockBrokenPath(Dockable dockable, DockingPort port, DockingConstants.Region region, SplitNode ctrlNode) {
		Component current = port.getDockedComponent();
		if (current instanceof JSplitPane) {
			return dockExtendedPath(dockable, port, region, ctrlNode);
		}
		
		if (current instanceof JTabbedPane) {
			return dock(dockable, port, DockingConstants.Region.CENTER, null);
		}
		
		Dockable embedded = findDockable(current);
		if (embedded == null || tabbed) {
			return dock(dockable, port, DockingConstants.Region.CENTER, null);
		}
		
		String embedId = embedded.getPersistentId();
		SplitNode lastNode = getLastNode();
		if (embedId.equals(lastNode.getSiblingId())) {
			return dock(dockable, port, getRegion(lastNode, current), lastNode);
		}
		else {

			return dock(dockable, port, region, ctrlNode);
		}
	}

	private boolean dockFullPath(Dockable dockable, DockingPort port, DockingConstants.Region region) {
		// the docking layout was altered since the last time our dockable we embedded within
		// it, and we were able to fill out the full docking path.  this means there is already
		// something within the target dockingPort where we expect to dock our dockable.
		
		// first, check to see if we need to use a tabbed layout
		Component current = port.getDockedComponent();
		if (current instanceof JTabbedPane) {
			return dock(dockable, port, DockingConstants.Region.CENTER, null);
		}
		
		// check to see if we dock outside the current port or outside of it
		Dockable docked = findDockable(current);
		if (docked != null) {
			Component comp = dockable.getComponent();
			if (port.isDockingAllowed(comp, DockingConstants.Region.CENTER)) {
				return dock(dockable, port, DockingConstants.Region.CENTER, null);
			}
			DockingPort superPort = (DockingPort) SwingUtilities.getAncestorOfClass(DockingPort.class, (Component) port);
			if (superPort != null) {
				return dock(dockable, superPort, region, getLastNode());
			}
			return dock(dockable, port, region, getLastNode());
		}
		
		// if we were't able to dock above, then the path changes means our current path
		// does not extend all the way down into to docking layout.  try to determine
		// an extended path and dock into it
		return dockExtendedPath(dockable, port, region, getLastNode());
	}

	private boolean dockExtendedPath(Dockable dockable, DockingPort port, DockingConstants.Region region, SplitNode ctrlNode) {
		Component docked = port.getDockedComponent();
		
		//I don't think this code will matter any more, given the null check, but leaving for now.
		//null is returned when a dockingport is empty, so we need to dock to an empty port
		
		// if 'docked' is not a split pane, then I don't know what it is.  let's print a
		// stacktrace and see who sends in an error report.
		if (docked != null && !(docked instanceof JSplitPane)) {
			Throwable t = new Throwable("Docked: " + docked);
			System.err.println("Exception: " + t.getMessage());
			return false;
		}
		
		//begin code that matters.
		
		SplitNode lastNode = getLastNode();
		String lastSibling = lastNode == null ? null : lastNode.getSiblingId();
		
		Set dockables = port.getDockables();
		for (Iterator it = dockables.iterator(); lastSibling != null && it.hasNext(); ) {
			Dockable d = (Dockable) it.next();
			if (d.getPersistentId().equals(lastSibling)) {
				DockingPort embedPort = d.getDockingPort();
				DockingConstants.Region embedRegion = getRegion(lastNode, d.getComponent());
				return dock(dockable, embedPort, embedRegion, ctrlNode);
			}
		}
		
		
		return dock(dockable, port, region, ctrlNode);
	}

	private DockingConstants.Region getRegion(SplitNode node, Component dockedComponent) {
		if (dockedComponent == null) {
			return DockingConstants.Region.CENTER;
		}
		return DockingUtility.getRegion(node.getRegion());
	}
	
	private SplitNode getLastNode() {
		return nodes.isEmpty() ? null : nodes.get(nodes.size() - 1);
	}

	private boolean dock(Dockable dockable, DockingPort port, DockingConstants.Region region, SplitNode ctrlNode) {
		boolean ret = DockingManager.dock(dockable, port, region);
		if (tabbed || ctrlNode == null) {
			return ret;
		}
		
		final float percent = ctrlNode.getPercentage();
		final Component docked = dockable.getComponent();

		EventQueue.invokeLater(() -> resizeSplitPane(docked, percent));
		return ret;
	}
	
	private void resizeSplitPane(Component comp, float percentage) {
		Container parent = comp.getParent();
		Container grandParent = parent == null ? null : parent.getParent();
		if (!(grandParent instanceof JSplitPane)) {
			return;
		}
		
		JSplitPane split = (JSplitPane) grandParent;
//              int splitSize = split.getOrientation()==DockingConstants.VERTICAL? split.getHeight(): split.getWidth();
//              int divLoc = (int)(percentage * (float)splitSize);
		split.setDividerLocation(percentage);
	}
	
	private static Dockable findDockable(Component c) {
		return DockingManager.getDockable(c);
	}
	
	private static Dockable findDockable(String id) {
		return DockingManager.getDockable(id);
	}
	
	private static boolean isDocked(Dockable dockable) {
		return DockingManager.isDocked(dockable);
	}
	
	private int getDepth() {
		return nodes.size();
	}
	
	public SplitNode getNode(int indx) {
		return indx < 0 || indx >= getDepth() ? null : nodes.get(indx);
	}
	
	@Override
	public Object clone() {
		ArrayList nodeList = null;
		if (nodes != null) {
			nodeList = new ArrayList(nodes.size());
			for (SplitNode node : nodes) {
				nodeList.add(node.clone());
			}
		}
		
		DockingPath path = new DockingPath(siblingId, tabbed, nodeList);
		path.rootPortId = rootPortId;
		return path;
	}
	
}
