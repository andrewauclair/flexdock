package org.flexdock.demos;

import org.flexdock.demos.util.DemoUtility;
import org.flexdock.docking.DockingConstants;
import org.flexdock.docking.DockingManager;
import org.flexdock.docking.DockingStub;
import org.flexdock.docking.defaults.DefaultDockingPort;
import org.flexdock.docking.drag.effects.EffectsManager;
import org.flexdock.docking.drag.preview.AlphaPreview;
import org.flexdock.util.SwingUtility;

import javax.swing.*;
import java.awt.*;

import static org.flexdock.util.SwingUtility.setSystemLookAndFeel;

/**
 * @author Andrew Auclair
 */
public class CoreDemo extends JFrame {
	public static class ToolWindow extends DockingPanel {
		
		ToolWindow(String name) {
			super(name);
		}
	}

	public static class DockingPanel extends JPanel implements DockingStub {
        private JPanel title;
		private final String name;
		
		DockingPanel(String name) {
			this.name = name;
			setLayout(new GridBagLayout());
			
			JPanel panel = new JPanel();
			panel.add(new JTextField("Test"));
			panel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

            title = new JPanel();
            title.add(new JLabel(name));

			GridBagConstraints gbc = new GridBagConstraints();
            gbc.weightx = 1.0;
            gbc.gridx = 0;
            gbc.gridy = 0;
            add(title, gbc);

			gbc.fill = GridBagConstraints.BOTH;
			gbc.weightx = 1.0;
			gbc.weighty = 1.0;
            gbc.gridx = 0;
            gbc.gridy = 1;
			add(panel, gbc);
		}
		
		@Override
		public Component getDragSource() {
            return title;
		}
		
		@Override
		public Component getFrameDragSource() {
            return title;
		}
		
		@Override
		public String getPersistentId() {
			return name;
		}
		
		@Override
		public String getTabText() {
			return name;
		}
		
		@Override
		public JComponent getComponent() {
			return this;
		}
	}

    static {
        EffectsManager.setPreview(new AlphaPreview(Color.black, new Color(119, 173, 255), 0.25f));
    }

	public static void main(String[] args) {
		setSystemLookAndFeel();
		
		DockingManager.setFloatingEnabled(true);

		SwingUtilities.invokeLater(() -> {
			CoreDemo demo = new CoreDemo();
			demo.setSize(800, 600);
			SwingUtility.centerOnScreen(demo);
			DemoUtility.setCloseOperation(demo);
			demo.setVisible(true);
		});
	}
	
	private CoreDemo() {
		DefaultDockingPort port = new DefaultDockingPort();
		
		JPanel fullPanel = new JPanel(new BorderLayout());
		fullPanel.add(port, BorderLayout.CENTER);
		
		DockingPanel panel = new DockingPanel("Test");
		DockingPanel panel2 = new DockingPanel("Test2");
		
		DockingManager.dock(panel, port);
		DockingManager.dock(panel2, panel, DockingConstants.Region.NORTH.toString());
		
		setContentPane(fullPanel);
	}
}
