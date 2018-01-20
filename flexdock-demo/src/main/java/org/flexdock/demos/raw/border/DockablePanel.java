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
package org.flexdock.demos.raw.border;

import org.flexdock.docking.Dockable;
import org.flexdock.docking.DockingManager;
import org.flexdock.docking.defaults.AbstractDockable;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class DockablePanel extends JPanel {
    private String title;
    private JPanel dragInit;
    private Dockable dockableImpl;

    public DockablePanel(String title) {
        super(new BorderLayout());
        dragInit = new JPanel();
        dragInit.setBackground(getBackground().darker());
        dragInit.setPreferredSize(new Dimension(10, 10));
        add(dragInit, BorderLayout.EAST);
        setBorder(new TitledBorder(title));
        setTitle(title);
		dockableImpl = new DockableImpl(this);
        DockingManager.registerDockable(dockableImpl);
    }

    private void setTitle(String title) {
        this.title = title;
    }

    Dockable getDockable() {
        return dockableImpl;
    }

    public String getTitle() {
        return title==null? null: title.trim();
    }

	private static class DockableImpl extends AbstractDockable {
		private final DockablePanel panel;

		private DockableImpl(DockablePanel panel) {
			super("dockable." + panel.getTitle());
			this.panel = panel;
            // the titlebar will the the 'hot' component that initiates dragging
			getDragSources().add(panel.dragInit);
			setTabText(panel.getTitle());
        }

        @Override
        public Component getComponent() {
			return panel;
        }
    }
}
