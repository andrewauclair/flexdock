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
package org.flexdock.view.dockbar;

import org.flexdock.docking.Dockable;
import org.flexdock.view.border.SlideoutBorder;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.ArrayList;


/**
 * @author Christopher Butler
 */
public class Dockbar extends JPanel {
	int orientation;
	protected DockbarManager manager;
	ArrayList<DockbarLabel> mDocks = new ArrayList<>();

	static {
		// make sure DockbarLabel is initialized
		Class<?> c = DockbarLabel.class;
	}

	public static int getValidOrientation(int orient) {
		switch (orient) {
			case SwingConstants.LEFT:
				return SwingConstants.LEFT;
			case SwingConstants.RIGHT:
				return SwingConstants.RIGHT;
			case SwingConstants.BOTTOM:
				return SwingConstants.BOTTOM;
			default:
				return SwingConstants.LEFT;
		}
	}

	Dockbar(DockbarManager manager, int orientation) {
		this.manager = manager;
		setBorder(new SlideoutBorder());
		setOrientation(orientation);
	}

	void undock(Dockable dockable) {
		DockbarLabel label = getLabel(dockable);

		remove(label);
		mDocks.remove(label);
		getParent().validate();
		repaint();
	}

	public DockbarLabel getLabel(Dockable dockable) {
		if (dockable == null) {
			return null;
		}

		for (DockbarLabel mDock : mDocks) {
			if (mDock.getDockable() == dockable) {
				return mDock;
			}
		} // for

		return null;
	}

	public boolean contains(Dockable dockable) {
		return getLabel(dockable) != null;
	}

	public void dock(Dockable dockable) {
		if (dockable == null) {
			return;
		}

		DockbarLabel currentLabel = getLabel(dockable);
		if (currentLabel != null) {
			currentLabel.setActive(false);
			return;
		}

		DockbarLabel newLabel = new DockbarLabel(dockable.getPersistentId(), getOrientation());

		LayoutManager layout = getLayout();
		add(newLabel);
		mDocks.add(newLabel);
	}

	public int getOrientation() {
		return orientation;
	}

	private void setOrientation(int orientation) {
		orientation = getValidOrientation(orientation);
		this.orientation = orientation;

		Border border = getBorder();
		if (border instanceof SlideoutBorder) {
			((SlideoutBorder) border).setOrientation(orientation);
		}

		int boxConstraint = orientation == SwingConstants.TOP ||
				orientation == SwingConstants.BOTTOM ? BoxLayout.LINE_AXIS : BoxLayout.PAGE_AXIS;
		setLayout(new BoxLayout(this, boxConstraint));
//		setLayout(new FlowLayout)
	}

	public void activate(String dockableId, boolean lock) {
		if (manager != null) {
			manager.setActiveDockable(dockableId);
			if (lock) {
				manager.getActivationListener().lockViewpane();
			}
		}
	}
}
