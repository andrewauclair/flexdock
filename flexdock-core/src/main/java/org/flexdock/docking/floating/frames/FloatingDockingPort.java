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
import org.flexdock.docking.DockingManager;
import org.flexdock.docking.defaults.DefaultDockingPort;
import org.flexdock.docking.event.DockingEvent;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;

import static org.flexdock.docking.DockingConstants.CENTER_REGION;

/**
 * @author Christopher Butler
 */
@SuppressWarnings(value = {"serial"})
public class FloatingDockingPort extends DefaultDockingPort {
	protected DockingFrame frame;
	private FrameDragListener dragListener;
	private final boolean useOwnListener;
	
	FloatingDockingPort(DockingFrame frame, String persistentId) {
		super(persistentId);
		getDockingProperties().setSingleTabsAllowed(false);
		setTabsAsDragSource(true); // TODO This should only happen if the frame is not decorated
		this.frame = frame;
		useOwnListener = frame.isUndecorated();

		dragListener = new FrameDragListener(frame); // TODO Should this only happen if the frame is not decorated?
	}

	@Override
	public boolean dock(Dockable dockable, String region) {
		boolean ret = super.dock(dockable, region);
		if (!useOwnListener) {
			return ret;
		}
		if (ret) {
			toggleListeners(dockable.getComponent(), true);
		}
		return ret;
	}

	@Override
	public boolean undock(Component comp) {
		boolean ret = super.undock(comp);
		if (!useOwnListener) {
			return ret;
		}
		if (ret) {
			toggleListeners(comp, false);
		}
		return ret;
	}

	@Override
	public void dragStarted(DockingEvent evt) {
		super.dragStarted(evt);

		if (!useOwnListener) {
			return;
		}
		Component dragSrc = (Component) evt.getTriggerSource();
		Dockable dockable = (Dockable) evt.getSource();

		boolean listenerEnabled = getFrameDragSources(dockable).contains(dragSrc);
		dragListener.setEnabled(listenerEnabled);
		if (listenerEnabled) {
			evt.consume();
		}
	}

	@Override
	public void undockingComplete(DockingEvent evt) {
		super.undockingComplete(evt);
		if (!useOwnListener) {
			return;
		}
		if (evt.getOldDockingPort() == this && getDockableCount() == 0) {
			frame.destroy();
			frame = null;
		}
	}

	private void toggleListeners(Component comp, boolean add) {
		Dockable dockable = DockingManager.getDockable(comp);
		if (add) {
			installListeners(dockable);
		}
		else {
			uninstallListeners(dockable);
		}
	}

	private void installListeners(Dockable dockable) {
		Set frameDraggers = getFrameDragSources(dockable);
		for (Object frameDragger : frameDraggers) {
			Component frameDragSrc = (Component) frameDragger;
			frameDragSrc.addMouseListener(dragListener);
			frameDragSrc.addMouseMotionListener(dragListener);
		}

		dockable.addDockingListener(this);
	}

	private void uninstallListeners(Dockable dockable) {
		Set frameDraggers = getFrameDragSources(dockable);
		for (Object frameDragger : frameDraggers) {
			Component frameDragSrc = (Component) frameDragger;
			frameDragSrc.removeMouseListener(dragListener);
			frameDragSrc.removeMouseMotionListener(dragListener);
		}
		dockable.removeDockingListener(this);
	}

	public int getDockableCount() {
		Component comp = getDockedComponent();
		if (!(comp instanceof JTabbedPane)) {
			return 0;
		}
		return ((JTabbedPane) comp).getTabCount();
	}

	protected Set<Component> getFrameDragSources(Dockable dockable) {
		return dockable == null ? new HashSet<>() : dockable.getFrameDragSources();
	}
}
