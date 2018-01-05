package org.flexdock.demos;

import org.flexdock.demos.util.DemoUtility;
import org.flexdock.docking.DockingManager;
import org.flexdock.docking.DockingStub;
import org.flexdock.docking.defaults.DefaultDockingPort;
import org.flexdock.util.SwingUtility;

import javax.swing.*;
import java.awt.*;

import static org.flexdock.util.SwingUtility.setSystemLookAndFeel;

public class CoreDemo extends JFrame {
	public class ToolWindow extends DockingPanel {
		
		ToolWindow(String name) {
			super(name);
		}
	}
	public class DockingPanel extends JPanel implements DockingStub {
		
		private final String name;

        DockingPanel(String name) {
			this.name = name;
			setLayout(new GridBagLayout());
			
			JPanel panel = new JPanel();
            panel.add(new JTextField("Test"));
            panel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
            
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.BOTH;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            add(panel, gbc);
		}
		
		@Override
		public Component getDragSource() {
			return this;
		}
		
		@Override
		public Component getFrameDragSource() {
			return this;
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
        DockingManager.dock(panel2, port);

        setContentPane(fullPanel);
	}
}
