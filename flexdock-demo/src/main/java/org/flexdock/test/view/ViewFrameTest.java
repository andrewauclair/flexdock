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
package org.flexdock.test.view;

import org.flexdock.docking.DockingConstants;
import org.flexdock.docking.floating.frames.DockingFrame;
import org.flexdock.util.SwingUtility;
import org.flexdock.view.View;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Christopher Butler
 */
public class ViewFrameTest extends JFrame implements ActionListener, DockingConstants {
    private DockingFrame dockingFrame;

    public static void main(String[] args) {
        SwingUtility.setPlaf("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        JFrame f = new ViewFrameTest();
        f.setBounds(100, 100, 100, 65);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
    }

    public ViewFrameTest() {
        super("ViewFrame Demo");

        Container c = getContentPane();
        c.setLayout(new FlowLayout());

        JButton b = new JButton("Float");
        b.addActionListener(this);
        c.add(b);

        dockingFrame = createDockingFrame();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(!dockingFrame.isVisible()) {
            dockingFrame.setSize(300, 300);
            SwingUtility.centerOnScreen(dockingFrame);
            dockingFrame.setVisible(true);
        }
    }

    private DockingFrame createDockingFrame() {
        DockingFrame frame = new DockingFrame("12345");
        frame.addDockable(createView("solution.explorer", "Solution Explorer"));
        frame.addDockable(createView("class.view", "Class View"));
        return frame;
    }

    private View createView(String id, String text) {
        View view = new View(id, text);
        view.addAction(createAction(CLOSE_ACTION, "Close"));
        view.addAction(createAction(PIN_ACTION, "Pin"));

        JPanel p = new JPanel();
        p.setBackground(Color.WHITE);
        p.setBorder(new LineBorder(Color.GRAY, 1));

        view.setContentPane(p);
        return view;
    }

    private Action createAction(String name, String tooltip) {
        Action a = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        };
        a.putValue(Action.NAME, name);
        a.putValue(Action.SHORT_DESCRIPTION, tooltip);
        return a;
    }

}
