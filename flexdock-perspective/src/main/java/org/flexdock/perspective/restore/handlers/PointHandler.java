/*
 * Copyright (c) 2005 FlexDock Development Team. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in the
 * Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE.
 */
package org.flexdock.perspective.restore.handlers;

import org.flexdock.docking.Dockable;
import org.flexdock.docking.DockingManager;
import org.flexdock.docking.DockingPort;
import org.flexdock.docking.state.DockingState;
import org.flexdock.perspective.RestorationManager;
import org.flexdock.util.NestedComponents;
import org.flexdock.util.RootWindow;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

import static org.flexdock.docking.DockingConstants.UNKNOWN_REGION;

/**
 * Created on 2005-05-26
 *
 * @author <a href="mailto:marius@eleritec.net">Christopher Butler</a>
 * @version $Id: PointHandler.java,v 1.5 2005-06-20 23:55:48 marius Exp $
 */
public class PointHandler implements RestorationHandler {

    @Override
    public boolean restore(Dockable dockable, DockingState dockingState, Map context) {
        if(DockingManager.isDocked(dockable)) {
            return false;
        }

        Component owner = RestorationManager.getRestoreContainer(dockable);
        return restoreDockable(dockable, owner, dockingState);
    }

    private boolean restoreDockable(Dockable dockable, Component win, DockingState dockingState) {
        RootWindow window = RootWindow.getRootContainer(win);
        Container contentPane = window.getContentPane();

        Point dropPoint = getDropPoint(dockable, contentPane, dockingState);
        if(dropPoint==null) {
            return false;
        }

        Component deep = SwingUtilities.getDeepestComponentAt(contentPane, dropPoint.x, dropPoint.y);
        NestedComponents dropTargets = NestedComponents.find(deep, Dockable.class, DockingPort.class);

        DockingPort port = dropTargets==null? null: (DockingPort)dropTargets.parent;
        Point mousePoint = port==null? null: SwingUtilities.convertPoint(contentPane, dropPoint, (Component)port);
        String region = port==null? UNKNOWN_REGION: port.getRegion(mousePoint);

        return DockingManager.dock(dockable, port, region);
    }

    private Point getDropPoint(Dockable dockable, Container contentPane, DockingState dockingState) {
        if(!dockingState.hasCenterPoint()) {
            return null;
        }

        float percentX = dockingState.getCenterX()/100f;
        float percentY = dockingState.getCenterY()/100f;

        Point dropPoint = new Point();
        dropPoint.x = Math.round(contentPane.getWidth() * percentX);
        dropPoint.y = Math.round(contentPane.getHeight() * percentY);

        return dropPoint;
    }

}
