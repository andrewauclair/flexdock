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
package org.flexdock.view;

import org.flexdock.docking.Dockable;
import org.flexdock.docking.DockingConstants;
import org.flexdock.docking.DockingManager;
import org.flexdock.docking.activation.ActiveDockableListener;
import org.flexdock.docking.defaults.DefaultDockingPort;
import org.flexdock.docking.defaults.StandardBorderManager;

import javax.swing.*;

/**
 * @author Christopher Butler
 */
public class Viewport extends DefaultDockingPort {

	static {
		// TODO This kind of stuff makes it really hard to write tests
		DockingManager.setDockingStrategy(Viewport.class, View.VIEW_DOCKING_STRATEGY);
	}

	public Viewport() {
		super();
		setBorderManager(new StandardBorderManager());
	}

	public Viewport(String portId) {
		super(portId);
		setBorderManager(new StandardBorderManager());
	}

//	@Override
//	public boolean isDockingAllowed(Component comp, DockingConstants.Region region) {
//		// if we're already blocked, then no need to interrogate
//		// the components in this dockingport
//		boolean blocked = !super.isDockingAllowed(comp, region);
//		if (blocked) {
//			return false;
//		}
//
//		// check to see if the region itself has been blocked for some reason
//		if (blockedRegions.contains(region)) {
//			return false;
//		}
//
//		// by default, allow docking in non-CENTER regions
//		if (region != DockingConstants.Region.CENTER) {
//			return true;
//		}
//
//		// allow docking in the CENTER if there's nothing already there,
//		// or if there's no Dockable associated with the component there
//		Dockable dockable = getCenterDockable();
//		if (dockable == null) {
//			return true;
//		}
//
//		// otherwise, only allow docking in the CENTER if the dockable
//		// doesn't mind
//		return !dockable.getDockingProperties().isTerritoryBlocked(region);
//	}

	public boolean dock(Dockable dockable) {
		return dock(dockable, DockingConstants.Region.CENTER);
	}

	@Override
	protected JTabbedPane createTabbedPane() {
		JTabbedPane pane = super.createTabbedPane();
		pane.addChangeListener(ActiveDockableListener.getInstance());
		return pane;
	}

	@Override
	protected String paramString() {
		return "id=" + getPersistentId() + "," + super.paramString();
	}
}
