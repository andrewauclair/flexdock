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
package org.flexdock.demos.raw;

import org.flexdock.demos.util.DemoUtility;
import org.flexdock.docking.Dockable;
import org.flexdock.docking.DockingManager;
import org.flexdock.docking.defaults.AbstractDockable;
import org.flexdock.docking.defaults.DefaultDockingPort;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

import static org.flexdock.docking.DockingConstants.CENTER_REGION;

public class CompoundDemo extends JPanel {
    private JLabel titlebar;
    private Dockable dockableImpl;

    private CompoundDemo(String title) {
        super();
        titlebar = createTitlebar(" " + title);
        add(titlebar);
        setBorder(new LineBorder(Color.black));
		dockableImpl = new DockableImpl(this);
    }

    private JLabel createTitlebar(String title) {
        JLabel lbl = new JLabel(title);
        lbl.setForeground(Color.white);
        lbl.setBackground(Color.blue);
        lbl.setOpaque(true);
        return lbl;
    }

    public String getTitle() {
        return titlebar.getText().trim();
    }

    @Override
    public void doLayout() {
        Insets in = getInsets();
        titlebar.setBounds(in.left, in.top, getWidth()-in.left-in.right, 25);
    }


    private Dockable getDockable() {
        return dockableImpl;
    }

	private static class DockableImpl extends AbstractDockable {
		private final CompoundDemo demo;

		private DockableImpl(CompoundDemo demo) {
			super("dockable." + demo.getTitle());
			this.demo = demo;
			// the titlebar will the the 'hot' component that initiates dragging
			getDragSources().add(demo.titlebar);
			setTabText(demo.getTitle());
		}

		@Override
		public Component getComponent() {
			return demo;
		}
	}

    private static JPanel createContentPane() {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.add(buildDockingPort("North"), BorderLayout.NORTH);
        p.add(buildDockingPort("South"), BorderLayout.SOUTH);
        p.add(buildDockingPort("East"), BorderLayout.EAST);
        p.add(buildDockingPort("West"), BorderLayout.WEST);
        p.add(createDockingPort(), BorderLayout.CENTER);
        return p;
    }

    private static DefaultDockingPort buildDockingPort(String desc) {
        // create the DockingPort
        DefaultDockingPort port = createDockingPort();

        // create the Dockable panel
        CompoundDemo cd = new CompoundDemo(desc);
        DockingManager.registerDockable(cd.getDockable());

        // dock the panel and return the DockingPort
        port.dock(cd.getDockable(), CENTER_REGION);
        return port;
    }

    private static DefaultDockingPort createDockingPort() {
        DefaultDockingPort port = new DefaultDockingPort();
        port.setBackground(Color.gray);
        port.setPreferredSize(new Dimension(100, 100));
        return port;
    }

    public static void main(String[] args) {
//                System.setSecurityManager(new SecurityManager());
        JFrame f = new JFrame("Compound Docking Demo");
        f.setContentPane(createContentPane());
        f.setSize(600, 400);
        DemoUtility.setCloseOperation(f);
        f.setVisible(true);
    }
}
