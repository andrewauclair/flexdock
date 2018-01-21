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
package org.flexdock.docking.floating.frames;

import org.flexdock.docking.Dockable;
import org.flexdock.docking.DockingConstants;
import org.flexdock.docking.DockingManager;
import org.flexdock.docking.DockingPort;
import org.flexdock.docking.state.FloatingGroup;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

/**
 * @author Andreas Ernst
 * @author Christopher Butler
 */
@SuppressWarnings(value = {"serial"})
public class DockingFrame extends JFrame {
	private static final BoundsMonitor BOUNDS_MONITOR = new BoundsMonitor();

	private FloatingDockingPort dockingPort;

	private String groupName;

    public DockingFrame(String groupName, boolean decorated) {
    	setUndecorated(!decorated);
		initialize(groupName);
	}

	private void initialize(String groupName) {
		// TODO I am not sure null should be passed here,
		// maybe we should use our IDPersistentIdProvider
		dockingPort = new FloatingDockingPort(this, null);
		setContentPane(dockingPort);
		this.groupName = groupName;
		addComponentListener(BOUNDS_MONITOR);
	}

	@Override
	protected JRootPane createRootPane() {
		return new RootPane(this);
	}

	public DockingPort getDockingPort() {
		return dockingPort;
	}

	public void addDockable(Dockable dockable) {
		if (dockable == null) {
			return;
		}

		dockingPort.dock(dockable, DockingConstants.Region.CENTER);
	}

	public void destroy() {
		setVisible(false);
		dockingPort = null;
		FloatingGroup group = getGroup();
		if (group != null) {
			group.setFrame(null);
		}
		dispose();
	}

	public String getGroupName() {
		return groupName;
	}

	public FloatingGroup getGroup() {
		return DockingManager.getFloatManager().getGroup(getGroupName());
	}

	private static class BoundsMonitor implements ComponentListener {

		@Override
		public void componentHidden(ComponentEvent e) {
		}

		@Override
		public void componentMoved(ComponentEvent e) {
			updateBounds(e);
		}

		@Override
		public void componentResized(ComponentEvent e) {
			updateBounds(e);
		}

		@Override
		public void componentShown(ComponentEvent e) {
			updateBounds(e);
		}

		private void updateBounds(ComponentEvent evt) {
			Component c = evt.getComponent();
			if (!(c instanceof DockingFrame)) {
				return;
			}

			DockingFrame frame = (DockingFrame) c;
			FloatingGroup group = frame.getGroup();
			if (group != null) {
				group.setBounds(frame.getBounds());
			}
		}
	}
}
