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
import org.flexdock.docking.state.DockingState;
import org.flexdock.docking.state.LayoutNode;
import org.flexdock.event.EventManager;
import org.flexdock.perspective.event.LayoutListener;
import org.flexdock.perspective.event.PerspectiveEvent;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author Mateusz Szczap
 */
public class Perspective implements Cloneable, Serializable {
	private String persistentId;
	private String perspectiveName;
	private Layout layout;
	private LayoutSequence initalSequence;
	
	/**
	 * @param persistentId
	 * @param perspectiveName
	 * @throws IllegalArgumentException if {@code persistentId} or {@code perspectiveName} is
	 *                                  {@code null}.
	 */
	public Perspective(String persistentId, String perspectiveName) {
		this.persistentId = Objects.requireNonNull(persistentId);
		this.perspectiveName = Objects.requireNonNull(perspectiveName);
		this.layout = new Layout();
	}
	
	public String getName() {
		return this.perspectiveName;
	}
	
	public String getPersistentId() {
		return this.persistentId;
	}
	
	public void addDockable(String dockableId) {
		this.layout.add(dockableId);
	}
	
	public boolean removeDockable(String dockableId) {
		return (this.layout.remove(dockableId) != null);
	}
	
	public Dockable getDockable(String dockableId) {
		return this.layout.getDockable(dockableId);
	}
	
	public void addLayoutListener(LayoutListener listener) {
		this.layout.addListener(listener);
	}
	
	public void removeLayoutListener(LayoutListener listener) {
		this.layout.removeListener(listener);
	}
	
	public Dockable[] getDockables() {
		return this.layout.getDockables();
	}
	
	public DockingState getDockingState(String dockable) {
		return this.layout.getDockingState(dockable, false);
	}
	
	public DockingState getDockingState(Dockable dockable) {
		return this.layout.getDockingState(dockable, false);
	}
	
	public DockingState getDockingState(String dockable, boolean load) {
		return this.layout.getDockingState(dockable, load);
	}
	
	public DockingState getDockingState(Dockable dockable, boolean load) {
		return this.layout.getDockingState(dockable, load);
	}
	
	public LayoutSequence getInitialSequence() {
		return getInitialSequence(false);
	}
	
	public LayoutSequence getInitialSequence(boolean create) {
		if (this.initalSequence == null && create) {
			this.initalSequence = new LayoutSequence();
		}
		return this.initalSequence;
	}
	
	public void setInitialSequence(LayoutSequence sequence) {
		this.initalSequence = sequence;
	}
	
	public Layout getLayout() {
		return this.layout;
	}
	
	public void setLayout(Layout layout) {
		this.layout = layout;
	}
	
	public void reset(DockingPort port) {
		if (this.initalSequence != null) {
			this.initalSequence.apply(port);

			Layout layout = this.layout;
			if (layout != null) {
				layout.update(this.initalSequence);
				EventManager.getInstance().dispatchEvent(new PerspectiveEvent(this, null, PerspectiveEvent.RESET));
			}
		}
	}
	
	public void load(DockingPort port) {
		Layout layout = this.layout;
		if (layout.isInitialized()) {
			layout.apply(port);
			EventManager.getInstance().dispatchEvent(new PerspectiveEvent(this, null, PerspectiveEvent.RESET));
		}
		else {
			reset(port);
		}
	}
	
	public void unload() {
		Dockable[] dockables = this.layout.getDockables();
		for (Dockable dockable : dockables) {
			DockingManager.close(dockable);
		}
	}
	
	public void cacheLayoutState(DockingPort port) {
		if (port != null) {
			Layout layout = this.layout;
			LayoutNode node = port.exportLayout();
			layout.setRestorationLayout(node);
		}
	}
	
	@Override
	public Object clone() {
		Perspective clone = new Perspective(this.persistentId, this.perspectiveName);
		clone.layout = (Layout) this.layout.clone();
		clone.initalSequence = this.initalSequence == null ? null : new LayoutSequence(this.initalSequence.getDockingStates());
		return clone;
	}
}
