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
package org.flexdock.test.perspective;

import org.flexdock.demos.util.VSNetStartPage;
import org.flexdock.docking.DockingManager;
import org.flexdock.util.SwingUtility;
import org.flexdock.view.View;
import org.flexdock.view.Viewport;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

import static org.flexdock.docking.DockingConstants.*;
import static org.flexdock.util.SwingUtility.setSystemLookAndFeel;

/**
 * @author Christopher Butler
 * @author Mateusz Szczap
 */
public class ViewRestorationTest extends JFrame {

    private static View view1 = null;
    private static View view2 = null;
    private static View view3 = null;
    private static View view4 = null;

    public static void main(String[] args) {
        setSystemLookAndFeel();

        JFrame f = new ViewRestorationTest();
        f.setSize(800, 600);
        SwingUtility.centerOnScreen(f);
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        f.setVisible(true);
    }

    private ViewRestorationTest() {
        super("Simple Show Viewport Demo");
        setContentPane(createContentPane());
        setJMenuBar(createApplicationMenuBar());
    }

    private JPanel createContentPane() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBorder(new EmptyBorder(5, 5, 5, 5));

        Viewport viewport = new Viewport();
        panel.add(viewport, BorderLayout.CENTER);

        View startPage = createStartPage();

        view1 = createView("solution.explorer", "Solution Explorer");
        view2 = createView("task.list", "Task List");
        view3 = createView("class.view", "Class View");
        view4 = createView("message.log", "Message Log");

        viewport.dock(startPage);
        startPage.dock(view1, WEST_REGION, 0.3f);
        startPage.dock(view2, SOUTH_REGION, 0.3f);
        startPage.dock(view4, EAST_REGION, 0.3f);
        view1.dock(view3, SOUTH_REGION, 0.3f);

        return panel;
    }

    private View createView(String id, String text) {
        View view = new View(id, text);
		view.getTitlebar().hidePin();

        JPanel p = new JPanel();
        p.setBorder(new LineBorder(Color.GRAY, 1));

        JTextField t = new JTextField(text);
        t.setPreferredSize(new Dimension(100, 20));
        p.add(t);
        view.setContentPane(p);

        return view;
    }

    private JMenuBar createApplicationMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu showViewMenu = new JMenu("Show View");

        showViewMenu.add(createShowViewActionFor(view1));
        showViewMenu.add(createShowViewActionFor(view2));
        showViewMenu.add(createShowViewActionFor(view3));
        showViewMenu.add(createShowViewActionFor(view4));

        menuBar.add(showViewMenu);

        return menuBar;
    }

    private Action createShowViewActionFor(View commonView) {
        ShowViewAction showViewAction = new ShowViewAction(commonView.getPersistentId());
        showViewAction.putValue(Action.NAME, commonView.getTitle());

        return showViewAction;
    }

    private class ShowViewAction extends AbstractAction {

        private String commonView;

        private ShowViewAction(String commonView) {
            this.commonView = commonView;
        }

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            DockingManager.display(commonView);
        }

    }

    private View createStartPage() {
        String id = "startPage";
		View view = new View(id, "");
        view.setTerritoryBlocked(CENTER_REGION, true);
        view.setContentPane(new VSNetStartPage());
        return view;
    }
}
