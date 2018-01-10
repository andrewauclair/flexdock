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
		// TODO Shouldn't we be able to allow single tabs? if they're document views maybe
		getDockingProperties().setSingleTabsAllowed(false);
		setTabsAsDragSource(true); // TODO This should only happen if the frame is not decorated
		this.frame = frame;
        useOwnListener = false;//frame.isUndecorated();

		dragListener = new FrameDragListener(frame);
	}

	@Override
	public boolean dock(Dockable dockable, String region) {
		boolean ret = super.dock(dockable, region);
		
		if (ret) {
			toggleListeners(dockable.getComponent(), true);
		}
		return ret;
	}

	@Override
	public boolean undock(Component comp) {
		boolean ret = super.undock(comp);
		
		if (ret) {
			toggleListeners(comp, false);
		}
		return ret;
	}

	@Override
	public void dragStarted(DockingEvent evt) {
		super.dragStarted(evt);
		
		// TODO This is kind of what we want. When dragging starts we want to undecorate the frame if it is decorated
//		EventQueue.invokeLater(() -> {
//			frame.dispose();
//			frame.setUndecorated(true);
//			frame.setVisible(true);
//		});
		
		if (!useOwnListener) {
			return;
		}
		
		Component dragSrc = (Component) evt.getTriggerSource();
		Dockable dockable = (Dockable) evt.getSource();

		boolean listenerEnabled = getFrameDragSources(dockable).contains(dragSrc);
		dragListener.setEnabled(listenerEnabled);
	}

	@Override
	public void undockingComplete(DockingEvent evt) {
		super.undockingComplete(evt);

        if (frame != null && getDockableCount() == 0) {
			frame.destroy();
			frame = null;
		}

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
		if (useOwnListener) {
			Set<Component> frameDraggers = getFrameDragSources(dockable);
			for (Component frameDragger : frameDraggers) {
				frameDragger.addMouseListener(dragListener);
				frameDragger.addMouseMotionListener(dragListener);
			}
		}

		dockable.addDockingListener(this);
	}

	private void uninstallListeners(Dockable dockable) {
		if (useOwnListener) {
			Set<Component> frameDraggers = getFrameDragSources(dockable);
			for (Component frameDragger : frameDraggers) {
				frameDragger.removeMouseListener(dragListener);
				frameDragger.removeMouseMotionListener(dragListener);
			}
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

    private Set<Component> getFrameDragSources(Dockable dockable) {
		return dockable == null ? new HashSet<>() : dockable.getFrameDragSources();
	}
}
