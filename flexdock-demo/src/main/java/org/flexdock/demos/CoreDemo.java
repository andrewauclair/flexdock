package org.flexdock.demos;

import org.flexdock.demos.util.DemoUtility;
import org.flexdock.docking.DockingManager;
import org.flexdock.docking.DockingStub;
import org.flexdock.util.SwingUtility;

import javax.swing.*;

import java.awt.*;

import static org.flexdock.util.SwingUtility.setSystemLookAndFeel;

public class CoreDemo extends JFrame {
	public class DockingPanel extends JPanel implements DockingStub {
		
		private final String name;
		
		public DockingPanel(String name) {
			this.name = name;
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
	
	public CoreDemo() {
	
	}
	
	
}
