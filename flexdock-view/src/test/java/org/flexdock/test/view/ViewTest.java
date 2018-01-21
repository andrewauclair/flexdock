/*
 * Created on Feb 26, 2005
 */
package org.flexdock.test.view;

import org.flexdock.docking.DockingConstants;
import org.flexdock.plaf.Configurator;
import org.flexdock.plaf.PlafManager;
import org.flexdock.plaf.XMLConstants;
import org.flexdock.plaf.theme.Theme;
import org.flexdock.plaf.theme.UIFactory;
import org.flexdock.util.SwingUtility;
import org.flexdock.view.View;
import org.flexdock.view.Viewport;
import org.jdesktop.swingx.JXLabel;
import org.w3c.dom.Element;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;

import static org.flexdock.util.SwingUtility.setSystemLookAndFeel;

/**
 * @author Christopher Butler
 * @author Claudio Romano
 */
public class ViewTest {

    private JList<Object> viewUIList;
    private JList<Object> titlebarUIList;
    private JList<Object> buttonUIList;
    private ThemeInfo themeInfo;

    public static void main(String[] args) {
        ViewTest windowTest = new ViewTest();
        setSystemLookAndFeel();
        windowTest.buildInterface();
    }

    private void buildInterface() {
        JFrame f = new JFrame();
        f.setJMenuBar(buildMenuBar());
        f.setContentPane(buildContent());
        f.setSize(new Dimension(800, 600));
        SwingUtility.centerOnScreen(f);
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        f.setVisible(true);
    }

    private JComponent buildContent() {
        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(new EmptyBorder(20, 20, 20, 20));
        content.setPreferredSize(new Dimension(800, 600));

        Viewport viewport = new Viewport();
        content.add(viewport, BorderLayout.CENTER);

        View view1 = buildView("themeinfo.view", "Theme Info", buildThemeInfoPane());
        View view2 = buildView("plafchooser.view", "Plaf Chooser", buidViewContentPane());

        viewport.dock(view2);
		view2.dock(view1, DockingConstants.Region.EAST, 0.2f);

        return content;
    }

    private View buildView(String id, String name, JComponent component) {
        View view = new View(id, name);
        view.setContentPane(component);

        return view;
    }


    private JMenuBar buildMenuBar() {
        JMenu menu;
        JMenuBar menuBar = new JMenuBar();
        menu = new JMenu("Available LookAndFeel's");

        LookAndFeelInfo[] lfInfos = UIManager.getInstalledLookAndFeels();
        for (LookAndFeelInfo lfInfo : lfInfos) {
            menu.add(new JMenuItem(new ChangeLookAndFeelAction(lfInfo)));
        }
        menuBar.add(menu);

        return menuBar;
    }

    private JComponent buidViewContentPane() {
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        contentPane.add(buildLists(), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.add(new JButton(new ChangePlafAction(this)));
        contentPane.add(buttonPanel, BorderLayout.SOUTH);

		JXLabel label = new JXLabel("JXLabel");
		label.setTextRotation(Math.PI / 2);

		contentPane.add(label, BorderLayout.NORTH);

        return contentPane;
    }

    private JComponent buildThemeInfoPane() {
        themeInfo = new ThemeInfo();
        //themeInfo.update(PlafManager.getPreferredTheme());
        return themeInfo.createPanel();
    }

    private JComponent buildLists() {
        viewUIList = new JList<>(getUIList("view-ui"));
        titlebarUIList = new JList<>(getUIList("titlebar-ui"));
        buttonUIList = new JList<>(getUIList("button-ui"));

        JPanel panel = new JPanel(new GridLayout(1, 3, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        panel.add(createUIListComp("View", viewUIList));
        panel.add(createUIListComp("Titlebar", titlebarUIList));
        panel.add(createUIListComp("Button", buttonUIList));

        return panel;
    }

    private JComponent createUIListComp(String name, JList uiList) {
        uiList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));


        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.LIGHT_GRAY);
        header.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        header.add(new JLabel(name), BorderLayout.WEST);

        uiList.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        uiList.setPreferredSize(new Dimension(170, 100));

        panel.add(header, BorderLayout.NORTH);
        panel.add(uiList, BorderLayout.CENTER);

        return panel;
    }

    private Object[] getUIList(String tagName) {
        List<String> tagNames = new ArrayList<>();
        HashMap elements = Configurator.getNamedElementsByTagName(tagName);
        for (Object o : Objects.requireNonNull(elements).keySet()) {
            Element elem = (Element) elements.get(o);
			tagNames.add(elem.getAttribute(XMLConstants.NAME_KEY));
        }

        return tagNames.toArray();
    }

	private static class ChangePlafAction extends AbstractAction {
		private final ViewTest test;

		ChangePlafAction(ViewTest test) {
			this.test = test;
            putValue(Action.NAME, "Apply custom theme");
        }

        @Override
		public void actionPerformed(ActionEvent e) {
            Properties p = new Properties();
			if (test.viewUIList.getSelectedValue() != null) {
				p.setProperty(UIFactory.VIEW_KEY, test.viewUIList.getSelectedValue().toString());
            }
			if (test.titlebarUIList.getSelectedValue() != null) {
				p.setProperty(UIFactory.TITLEBAR_KEY, test.titlebarUIList.getSelectedValue().toString());
            }
			if (test.buttonUIList.getSelectedValue() != null) {
				p.setProperty(UIFactory.BUTTON_KEY, test.buttonUIList.getSelectedValue().toString());
            }

            Theme theme = PlafManager.setCustomTheme("custom.theme", p);
            PlafManager.setPreferredTheme("custom.theme", true);
			test.themeInfo.update(theme);
        }

    }

	private static class ChangeLookAndFeelAction extends AbstractAction {

        private LookAndFeelInfo lfInfo;

        private ChangeLookAndFeelAction(LookAndFeelInfo lfInfo) {
            this.lfInfo = lfInfo;
            putValue(Action.NAME, lfInfo.getName());
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            SwingUtility.setPlaf(lfInfo.getClassName());
            PlafManager.setPreferredTheme("custom.theme", true);
        }

    }

    private static class EmptyAction extends AbstractAction {
        private EmptyAction(String name) {
            putValue(Action.NAME, name);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
        }
    }

    public static class ThemeInfo {
        private final JLabel view = new JLabel("View:");
        private final JLabel titlebar = new JLabel("Titlebar:");
        private final JLabel button = new JLabel("Button:");
        private final JLabel vView = new JLabel("");
        private final JLabel vTitlebar = new JLabel("");
        private final JLabel vButton = new JLabel("");

        JPanel createPanel() {

            JPanel panel = new JPanel(null) {
                @Override
                public void doLayout() {
                    int x = 10;
                    int row = 1;
                    int rowInc = 22;
                    int labelWeight = 60 + 10;
                    int valueWidth = 120;
                    int height = (int) view.getPreferredSize().getHeight();

                    view.setBounds(x, row * rowInc, labelWeight, height);
                    vView.setBounds(labelWeight + 10, row * rowInc, valueWidth, height);
                    row++;
                    titlebar.setBounds(x, row * rowInc, labelWeight, height);
                    vTitlebar.setBounds(labelWeight + 10, row * rowInc, valueWidth, height);
                    row++;
                    button.setBounds(x, row * rowInc, labelWeight, height);
                    vButton.setBounds(labelWeight + 10, row * rowInc, valueWidth, height);

                    setPreferredSize(new Dimension(400, 100));
                }
            };

            panel.add(view);
            panel.add(vView);
            panel.add(titlebar);
            panel.add(vTitlebar);
            panel.add(button);
            panel.add(vButton);

            return panel;
        }

        public void update(Theme theme) {
            vView.setText(theme.getViewUI().getCreationParameters().getName());
            vButton.setText(theme.getButtonUI().getCreationParameters().getName());
        }
    }

}