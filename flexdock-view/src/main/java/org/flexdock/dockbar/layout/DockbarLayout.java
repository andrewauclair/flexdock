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
package org.flexdock.dockbar.layout;

import org.flexdock.dockbar.Dockbar;
import org.flexdock.dockbar.DockbarManager;
import org.flexdock.dockbar.ViewPane;
import org.flexdock.docking.Dockable;
import org.flexdock.docking.props.DockablePropertySet;
import org.flexdock.util.Utilities;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;

/**
 * TODO I would like to replace this with a vertical/horizontal flow layout with JXLabels
 * @author Christopher Butler
 */
public class DockbarLayout {
    public static final int MINIMUM_VIEW_SIZE = 20;
    private static final int[] EDGES = {
			SwingConstants.LEFT, SwingConstants.RIGHT,
			SwingConstants.BOTTOM
    };

    private DockbarManager manager;
    private JComponent leftEdgeGuide;
    private JComponent rightEdgeGuide;
    private JComponent bottomEdgeGuide;

    public DockbarLayout(DockbarManager mgr) {
        manager = mgr;
    }



    public void layout() {
        // makes sure our current insets are up to date before trying to layout
        // the dockbars.  otherwise, they will be layed out with improper bounds.
        updateInsets();

        Rectangle rect = DockbarLayoutManager.getManager().getLayoutArea(manager);
        int rightX = rect.x + rect.width;
        int bottomY = rect.y + rect.height;

        Dockbar leftBar = manager.getLeftBar();
        Dockbar rightBar = manager.getRightBar();
        Dockbar bottomBar = manager.getBottomBar();

        Dimension leftPref = leftBar.getPreferredSize();
        Dimension rightPref = rightBar.getPreferredSize();
        Dimension bottomPref = bottomBar.getPreferredSize();

        // set the dockbar bounds
        leftBar.setBounds(rect.x, rect.y, leftPref.width, rect.height-bottomPref.height);
        rightBar.setBounds(rightX-rightPref.width, rect.y, rightPref.width, rect.height-bottomPref.height);
        // use complete window width for proper statusbar support
        bottomBar.setBounds(rect.x, bottomY-bottomPref.height, rect.width, bottomPref.height);
        layoutViewpane();
    }


    public int getDesiredViewpaneSize() {
        Dockable dockable = manager.getActiveDockable();
        if(dockable==null) {
            return 0;
        }

		Rectangle rect = DockbarLayoutManager.getManager().getViewArea(manager);
        DockablePropertySet props = dockable.getDockingProperties();

        // determine what percentage of the viewable area we want the viewpane to take up
        float viewSize = props.getPreviewSize().floatValue();
        int edge = manager.getActiveEdge();
		if (edge == SwingConstants.LEFT || edge == SwingConstants.RIGHT) {
            return (int)(rect.width*viewSize);
        }
        return (int)(rect.height*viewSize);
    }

    protected void layoutViewpane() {
        ViewPane viewPane = manager.getViewPane();
        Dockable dockable = manager.getActiveDockable();
        if(dockable==null) {
            viewPane.setBounds(0, 0, 0, 0);
            return;
        }

        int edge = manager.getActiveEdge();
        int viewpaneSize = viewPane.getPrefSize();
        if(viewpaneSize==ViewPane.UNSPECIFIED_PREFERRED_SIZE) {
            viewpaneSize = getDesiredViewpaneSize();
        }

		Rectangle rect = DockbarLayoutManager.getManager().getViewArea(manager);
		if (edge == SwingConstants.LEFT || edge == SwingConstants.RIGHT) {
			if (edge == SwingConstants.RIGHT) {
                rect.x = rect.x + rect.width - viewpaneSize;
            }
            rect.width = viewpaneSize;
        } else {
			if (edge == SwingConstants.BOTTOM) {
                rect.y = rect.y + rect.height - viewpaneSize;
            }
            rect.height = viewpaneSize;
        }
        viewPane.setBounds(rect);
    }


    private void updateInsets() {
        Insets emptyInsets = getEmptyInsets();
        boolean changed = resetGuideBorders();

        HashSet borderSet = new HashSet(3);
		add(borderSet, getInsetBorder(SwingConstants.LEFT));
		add(borderSet, getInsetBorder(SwingConstants.RIGHT));
		add(borderSet, getInsetBorder(SwingConstants.BOTTOM));

        HashSet guideSet = new HashSet(3);
		add(guideSet, getCurrentEdgeGuide(SwingConstants.LEFT));
		add(guideSet, getCurrentEdgeGuide(SwingConstants.RIGHT));
		add(guideSet, getCurrentEdgeGuide(SwingConstants.BOTTOM));

		for (Object aBorderSet : borderSet) {
			InsetBorder border = (InsetBorder) aBorderSet;
			changed = border.setEmptyInsets(emptyInsets) || changed;
		}

        if(changed) {
			for (Object aGuideSet : guideSet) {
				JComponent guide = (JComponent) aGuideSet;
				guide.revalidate();
			}
        }
    }

    private boolean resetGuideBorders() {
		boolean changed = resetGuide(SwingConstants.LEFT);
		changed = resetGuide(SwingConstants.RIGHT) || changed;
		changed = resetGuide(SwingConstants.BOTTOM) || changed;
	
		toggleInsetBorder(SwingConstants.LEFT);
		toggleInsetBorder(SwingConstants.RIGHT);
		toggleInsetBorder(SwingConstants.BOTTOM);

        return changed;
    }

    private boolean resetGuide(int edge) {
        JComponent currGuide = getCurrentEdgeGuide(edge);
        JComponent newGuide = queryEdgeGuide(edge);
        boolean changed = false;

        if(Utilities.isChanged(currGuide, newGuide)) {
            changed = true;
            // if the edge-guide has changed, then we're going to try to remove the old
            // InsetBorder from the previous edge-guide
            Border b = currGuide==null? null: currGuide.getBorder();
            InsetBorder fakeBorder = b instanceof InsetBorder? (InsetBorder)b: null;
            // check to see if the previous edge-guide already had an InsetBorder
            if(fakeBorder!=null) {
                // replace the InsetBorder with the old edge-guide's real border
                Border realBorder = fakeBorder.getWrappedBorder();
                currGuide.setBorder(realBorder);
                fakeBorder.toggleEdge(edge, false);
            }
        }

        // if there is no new edge-guide, then we can't set an InsetBorder for it
        if(newGuide==null) {
            return setCurrentEdgeGuide(edge, newGuide) || changed;
        }

        Border border = newGuide.getBorder();
        // if the new edge-guide doesn't have an InsetBorder, then install one
        if(!(border instanceof InsetBorder)) {
            changed = true;
            InsetBorder insetBorder = InsetBorder.createBorder(border, true, new Insets(-1, -1, -1, -1));
            newGuide.setBorder(insetBorder);
        }

        // make sure our edge tracking on the InsetBorder is cleared out.
        // we will rebuild it later on
        InsetBorder insetBorder = (InsetBorder)newGuide.getBorder();
        insetBorder.clearEdges();

        // update the edge-guide reference
        return setCurrentEdgeGuide(edge, newGuide) || changed;
    }

    private void toggleInsetBorder(int edge) {
        InsetBorder border = getInsetBorder(edge);
        if(border!=null) {
            border.toggleEdge(edge, true);
        }
    }

    private InsetBorder getInsetBorder(int edge) {
        JComponent comp = getCurrentEdgeGuide(edge);
        Border border = comp==null? null: comp.getBorder();
        return border instanceof InsetBorder? (InsetBorder)border: null;
    }


    private Insets getEmptyInsets() {
        return new Insets(0, getLeftInset(), getBottomInset(), getRightInset());
    }

    private int getLeftInset() {
        return getDockbarInset(manager.getLeftBar());
    }

    private int getRightInset() {
        return getDockbarInset(manager.getRightBar());
    }

    private int getBottomInset() {
        return getDockbarInset(manager.getBottomBar());
    }

    private int getDockbarInset(Dockbar dockbar) {
        boolean visible = dockbar.isVisible();
        if(!visible) {
            return 0;
        }

        Dimension dim = dockbar.getPreferredSize();
        if(dockbar==manager.getLeftBar() || dockbar==manager.getRightBar()) {
            return dim.width;
        }
        return dim.height;
    }

    private JComponent queryEdgeGuide(int constraint) {
        return DockbarLayoutManager.getManager().getEdgeGuide(manager, constraint);
    }

    private JComponent getCurrentEdgeGuide(int constraint) {
        switch(constraint) {
		case SwingConstants.LEFT:
                return leftEdgeGuide;
		case SwingConstants.RIGHT:
                return rightEdgeGuide;
		case SwingConstants.BOTTOM:
                return bottomEdgeGuide;
        }
        return null;
    }

    private boolean setCurrentEdgeGuide(int constraint, JComponent comp) {
        boolean changed = getCurrentEdgeGuide(constraint)==comp;
        switch(constraint) {
		case SwingConstants.LEFT:
                leftEdgeGuide = comp;
                break;
		case SwingConstants.RIGHT:
                rightEdgeGuide = comp;
                break;
		case SwingConstants.BOTTOM:
                bottomEdgeGuide = comp;
                break;
        }
        return changed;
    }

    private void add(Set set, Object obj) {
        if(obj!=null) {
            set.add(obj);
        }
    }
}
