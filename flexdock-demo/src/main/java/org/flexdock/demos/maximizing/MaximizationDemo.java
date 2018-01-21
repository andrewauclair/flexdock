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
package org.flexdock.demos.maximizing;

import com.jgoodies.looks.plastic.PlasticXPLookAndFeel;
import org.flexdock.demos.util.DemoUtility;
import org.flexdock.docking.Dockable;
import org.flexdock.docking.DockingConstants;
import org.flexdock.docking.DockingManager;
import org.flexdock.util.SwingUtility;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class MaximizationDemo {

    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(new PlasticXPLookAndFeel());
        } catch (UnsupportedLookAndFeelException e) {
            throw new RuntimeException(e);
        }

        boolean loaded = configureDocking();

        JFrame frame = new JFrame("FlexPortalMaximized");

        frame.setContentPane(createContentPane(loaded));

        DemoUtility.setCloseOperation(frame);

        frame.setSize(500, 500);
        SwingUtility.centerOnScreen(frame);
        // frame.pack();
        frame.setVisible(true);
    }

    private static boolean configureDocking() {
        DockingManager.setFloatingEnabled(false);
        return false;
    }

    private static JComponent createContentPane(boolean loaded) {

        MyDockingPort dockingPort = new MyDockingPort();

        Dockable topComp = createFramePanel("Top");
        DockingManager.registerDockable(topComp);
		DockingManager.dock(topComp, dockingPort, DockingConstants.Region.CENTER);

        Dockable south = createFramePanel("South");
        DockingManager.registerDockable(south);
		DockingManager.dock(south, topComp, DockingConstants.Region.SOUTH, 0.3f);

        Dockable west = createFramePanel("West");
        DockingManager.registerDockable(west);
		DockingManager.dock(west, topComp, DockingConstants.Region.WEST, 0.5f);

        Dockable l2South = createFramePanel("South of West");
        DockingManager.registerDockable(l2South);
		DockingManager.dock(l2South, west, DockingConstants.Region.SOUTH, 0.33f);

        Dockable east = createFramePanel("East");
        DockingManager.registerDockable(east);
		DockingManager.dock(east, topComp, DockingConstants.Region.EAST, 0.2f);

        return dockingPort;
    }

    private static Dockable createFramePanel(String title) {
        JLabel label = new JLabel("Content of " + title);
		JButton maxButton = createButton(new ImageIcon(createImageImpl("maximize.gif")));
        JToolBar toolbar = createPortletToolbar(maxButton);
        SimpleInternalFrame sif = new SimpleInternalFrame(title, toolbar, label);
        final Dockable dockable = new DockableSimpleInternalFrame(sif);

		maxButton.addActionListener(e -> {
			//System.out.println("Calling DockingManager to maximize: " + dockable);
			DockingManager.toggleMaximized(dockable);
		});

        return dockable;
    }

    private static JButton createButton(Icon icon) {
        JButton button = new JButton(icon);
        button.setFocusable(false);
        return button;
    }

    private static JToolBar createPortletToolbar(JButton maxButton) {
        JToolBar toolbar = new JToolBar();
        toolbar.add(maxButton);
        toolbar.setFloatable(false);
        toolbar.putClientProperty("JToolBar.isRollover", Boolean.TRUE);

        return toolbar;
    }

	private static Image createImageImpl(String resourceName) {
        URL iconURL = MaximizationDemo.class.getResource(resourceName);
        if (iconURL == null) {
            throw new RuntimeException("Could not find: " + resourceName);
        }
        return Toolkit.getDefaultToolkit().createImage(iconURL);
    }

}