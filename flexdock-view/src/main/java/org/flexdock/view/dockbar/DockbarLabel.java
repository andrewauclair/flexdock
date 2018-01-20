/* Copyright (c) 2005 Andreas Ernst

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in the
Software without restriction, including without limitation the rights to use,
copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
Software, and to permit persons to whom the Software is furnished to do so, subject
to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. */
package org.flexdock.view.dockbar;

import org.flexdock.docking.Dockable;
import org.flexdock.docking.DockingManager;
import org.flexdock.docking.props.DockablePropertySet;
import org.flexdock.plaf.common.border.RoundedLineBorder;
import org.flexdock.view.dockbar.util.TextIcon;
import org.jdesktop.swingx.JXLabel;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * @author Andreas Ernst
 * @author Christopher Butler
 */
public class DockbarLabel extends JXLabel implements MouseListener {
	private static final Insets[] INSETS = createInsets();
	private static final int[] ROTATIONS = createRotations();

	// instance data
	private String dockingId;

	//        private boolean mDragging = false;
	private RoundedLineBorder mBorder;
	private boolean mActive = false;
	private int mDefaultOrientation;


	private static Insets[] createInsets() {
		Insets[] insets = new Insets[5];
		insets[SwingUtilities.CENTER] = new Insets(1, 1, 1, 1);
		insets[SwingUtilities.LEFT] = new Insets(1, 1, 2, 1);
		insets[SwingUtilities.RIGHT] = new Insets(1, 1, 2, 1);
		insets[SwingUtilities.TOP] = new Insets(1, 1, 1, 2);
		insets[SwingUtilities.BOTTOM] = new Insets(1, 1, 1, 2);
		return insets;
	}

	private static int[] createRotations() {
		int[] rotations = new int[5];
		rotations[SwingUtilities.CENTER] = TextIcon.ROTATE_NONE;
		rotations[SwingUtilities.LEFT] = TextIcon.ROTATE_LEFT;
		rotations[SwingUtilities.RIGHT] = TextIcon.ROTATE_RIGHT;
		rotations[SwingUtilities.TOP] = TextIcon.ROTATE_NONE;
		rotations[SwingUtilities.BOTTOM] = TextIcon.ROTATE_NONE;
		return rotations;
	}

	DockbarLabel(String dockableId, int defaultOrientation) {
		dockingId = dockableId;

		mDefaultOrientation = Dockbar.getValidOrientation(defaultOrientation);
		mBorder = new RoundedLineBorder(Color.lightGray, 3);
		setBorder(new CompoundBorder(new EmptyBorder(new Insets(1, 1, 1, 1)), mBorder));

		addMouseListener(this);

		TextIcon icon = new TextIcon(this, 2, 1);
		setIcon(icon);
		updateIcon();
		icon.validate();


	}

	// stuff

	@Override
	public Border getBorder() {
		boolean mInPaint = false;
		return mInPaint ? null : super.getBorder();
	}

	public void setActive(boolean active) {
		if (mActive != active) {
			mActive = active;

			updateBorder();

			//repaint();
		} // if
	}

	private void updateBorder() {
		mBorder.setFilled(mActive);
	}

	// protected

	private void activate(boolean lock) {
		Dockbar dockbar = (Dockbar) SwingUtilities.getAncestorOfClass(Dockbar.class, this);
		if (dockbar != null) {
			dockbar.activate(dockingId, lock);
		}
	}

	@Override
	public Dimension getMinimumSize() {
		return getPreferredSize();
	}

	private void updateIcon() {
		Object obj = getIcon();
		if (!(obj instanceof TextIcon)) {
			//return;
		}

		Dockable d = getDockable();
		DockablePropertySet p = d == null ? null : d.getDockingProperties();
		if (p == null) {
			return;
		}

		int orientation = getOrientation();
		int rotation = ROTATIONS[orientation];
		Icon dockIcon = p.getDockbarIcon();
		if (dockIcon == null) {
			dockIcon = p.getTabIcon();
		}

		TextIcon icon = (TextIcon) obj;
		icon.setIcon(dockIcon);
		icon.setText("old icon");
		icon.setRotation(rotation);

		setText(p.getDockableDesc());

		if (orientation == TextIcon.ROTATE_LEFT) {
			setTextRotation(3 * Math.PI / 2);
		}
		else if (orientation == TextIcon.ROTATE_RIGHT) {
			setTextRotation(Math.PI / 2);
		}
	}

	public Dockable getDockable() {
		return DockingManager.getDockable(dockingId);
	}

	private int getOrientation() {
		Container cnt = getParent();
		if (cnt instanceof Dockbar) {
			return ((Dockbar) cnt).getOrientation();
		}
		return mDefaultOrientation;
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getButton() != MouseEvent.BUTTON1) {
			return;
		}

		activate(true);
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		activate(false);
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}
}

