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
package org.flexdock.demos.perspective;

import org.flexdock.demos.util.DemoUtility;
import org.flexdock.docking.DockableFactory;
import org.flexdock.docking.DockingManager;
import org.flexdock.docking.state.PersistenceException;
import org.flexdock.perspective.LayoutSequence;
import org.flexdock.perspective.Perspective;
import org.flexdock.perspective.PerspectiveFactory;
import org.flexdock.perspective.PerspectiveManager;
import org.flexdock.perspective.actions.OpenPerspectiveAction;
import org.flexdock.perspective.persist.FilePersistenceHandler;
import org.flexdock.perspective.persist.PersistenceHandler;
import org.flexdock.util.ResourceManager;
import org.flexdock.util.SwingUtility;
import org.flexdock.view.View;
import org.flexdock.view.Viewport;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.IOException;

import static org.flexdock.docking.DockingConstants.Region;

/**
 * Created on 2005-04-17
 *
 * @author <a href="mailto:mati@sz.home.pl">Mateusz Szczap</a>
 * @version $Id: PerspectivesDemo.java,v 1.17 2005-10-09 21:09:39 eeaston Exp $
 */
public class PerspectivesDemo extends JFrame {
	private static final long serialVersionUID = 1L;

	private static final String PERSPECTIVE_FILE = "PerspectiveDemo.data";
	private static final String MAIN_VIEW = "main.view";
	private static final String BIRD_VIEW = "bird.view";
	private static final String MESSAGE_VIEW = "message.log";
	private static final String PROBLEM_VIEW = "problem";
	private static final String CONSOLE_VIEW = "console";

	private static final String P1 = "p1";
	private static final String P2 = "p2";
	private static final String P3 = "p3";

	public static void main(String[] args) {
		SwingUtility.setPlaf(UIManager.getSystemLookAndFeelClassName());

		// setup the flexdock configuration
		configureDocking();

		// create and show the GUI
		EventQueue.invokeLater(PerspectivesDemo::runGUI);
	}

	private static void runGUI() {
		// create out application frame
		PerspectivesDemo flexDockDemo = new PerspectivesDemo();
		flexDockDemo.setSize(800, 600);
		SwingUtility.centerOnScreen(flexDockDemo);
		DemoUtility.setCloseOperation(flexDockDemo);
		// load the current layout state into the application frame
		DockingManager.restoreLayout();
		// now show the frame
		flexDockDemo.setVisible(true);
	}


	private PerspectivesDemo() {
		super("FlexDock Demo");
		setContentPane(createContentPane());
		setJMenuBar(createApplicationMenuBar());
	}

	private JPanel createContentPane() {
		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		//tworzymy glowny view port do dokowania
		Viewport viewport = new Viewport();
//        Border outerBorder = BorderFactory.createEmptyBorder(0,0,5,5);
//        Border innerBorder = new ShadowBorder();
//        viewport.setBorderManager(new StandardBorderManager(BorderFactory.createCompoundBorder(outerBorder, innerBorder)));

//        viewport.setBorder(new EmptyBorder(10, 10, 10, 10));

		//rejestrujemy glowny view port

		contentPane.add(viewport, BorderLayout.CENTER);
		return contentPane;
	}

	private JMenuBar createApplicationMenuBar() {
		JMenuBar menuBar = new JMenuBar();

		JMenu showViewMenu = new JMenu("Show View");

		JMenuItem bird = new JMenuItem(View.getInstance(BIRD_VIEW).getTitle());
		showViewMenu.add(bird);
		JMenuItem msg = new JMenuItem(View.getInstance(MESSAGE_VIEW).getTitle());
		showViewMenu.add(msg);
		JMenuItem problem = new JMenuItem(View.getInstance(PROBLEM_VIEW).getTitle());
		showViewMenu.add(problem);
		JMenuItem console = new JMenuItem(View.getInstance(CONSOLE_VIEW).getTitle());
		showViewMenu.add(console);

		bird.addActionListener(e -> DockingManager.display(View.getInstance(BIRD_VIEW)));
		msg.addActionListener(e -> DockingManager.display(View.getInstance(MESSAGE_VIEW)));
		problem.addActionListener(e -> DockingManager.display(View.getInstance(PROBLEM_VIEW)));
		console.addActionListener(e -> DockingManager.display(View.getInstance(CONSOLE_VIEW)));

		JMenu perspectiveMenu = new JMenu("Perspective");
		//pobieramy perspektywe nr 1
		perspectiveMenu.add(new OpenPerspectiveAction(P1));
		perspectiveMenu.add(new OpenPerspectiveAction(P2));
		perspectiveMenu.add(new OpenPerspectiveAction(P3));

		menuBar.add(showViewMenu);
		menuBar.add(perspectiveMenu);

		return menuBar;
	}

	private static void configureDocking() {
		// setup the DockingManager to work with our application
		DockingManager.setDockableFactory(new ViewFactory());
		DockingManager.setFloatingEnabled(true);

		// configure the perspective manager
		PerspectiveManager.setFactory(new DemoPerspectiveFactory());
		PerspectiveManager.setRestoreFloatingOnLoad(true);
		PerspectiveManager mgr = PerspectiveManager.getInstance();
		mgr.setCurrentPerspective(P3, true);

		// load any previously persisted layouts
		PersistenceHandler persister = FilePersistenceHandler.createDefault(PERSPECTIVE_FILE);
		PerspectiveManager.setPersistenceHandler(persister);
		try {
			DockingManager.loadLayoutModel();
		}
		catch (IOException | PersistenceException e) {
			e.printStackTrace();
		}
		// remember to store on shutdown
		DockingManager.setAutoPersist(true);
	}

	private static class DemoPerspectiveFactory implements PerspectiveFactory {

		@Override
		public Perspective getPerspective(String persistentId) {
			if (P1.equals(persistentId)) {
				return createPerspective1();
			}
			if (P2.equals(persistentId)) {
				return createPerspective2();
			}
			if (P3.equals(persistentId)) {
				return createPerspective3();
			}
			return null;
		}

		private Perspective createPerspective1() {
			Perspective perspective = new Perspective(P1, "Perspective1");
			LayoutSequence sequence = perspective.getInitialSequence(true);

			sequence.add(MAIN_VIEW);
			sequence.add(BIRD_VIEW, MAIN_VIEW, Region.EAST, 0.3f);
			sequence.add(MESSAGE_VIEW, MAIN_VIEW, Region.WEST, 0.3f);
			sequence.add(PROBLEM_VIEW, MESSAGE_VIEW);
			sequence.add(CONSOLE_VIEW, MESSAGE_VIEW);

			return perspective;
		}

		private Perspective createPerspective2() {
			Perspective perspective = new Perspective(P2, "Perspective2");
			LayoutSequence sequence = perspective.getInitialSequence(true);

			sequence.add(MAIN_VIEW);
			sequence.add(BIRD_VIEW, MAIN_VIEW, Region.WEST, 0.3f);
			sequence.add(MESSAGE_VIEW, BIRD_VIEW, Region.SOUTH, 0.5f);
			sequence.add(PROBLEM_VIEW, MESSAGE_VIEW);
			sequence.add(CONSOLE_VIEW, MESSAGE_VIEW, Region.EAST, 0.5f);

			return perspective;
		}

		private Perspective createPerspective3() {
			Perspective perspective = new Perspective(P3, "Perspective3");
			LayoutSequence sequence = perspective.getInitialSequence(true);
			sequence.add(MAIN_VIEW);

			return perspective;
		}
	}

	private static class ViewFactory extends DockableFactory.Stub {

		@Override
		public Component getDockableComponent(String dockableId) {
			if (MAIN_VIEW.equals(dockableId)) {
				return createMainView();
			}
			if (BIRD_VIEW.equals(dockableId)) {
				return createView(BIRD_VIEW, "Bird View", "birdView001.png");
			}
			if (MESSAGE_VIEW.equals(dockableId)) {
				return createView(MESSAGE_VIEW, "Message Log", "msgLog001.png");
			}
			if (PROBLEM_VIEW.equals(dockableId)) {
				return createView(PROBLEM_VIEW, "Problems", "problems001.png");
			}
			if (CONSOLE_VIEW.equals(dockableId)) {
				return createView(CONSOLE_VIEW, "Console", "console001.png");
			}
			return null;
		}

		private View createView(String id, String text, String iconName) {
			View view = new View(id, text);
			//Dodajemy akcje close to tego view
//			view.addAction(DockingConstants.PIN_ACTION);
//			view.addAction(DockingConstants.CLOSE_ACTION);

			JPanel panel = new JPanel();
			panel.setBorder(new LineBorder(Color.GRAY, 1));

			JTextField textField = new JTextField(text);
			textField.setPreferredSize(new Dimension(100, 20));
			panel.add(textField);
			view.setContentPane(panel);

			Icon icon = ResourceManager.createIcon("org/flexdock/demos/view/" + iconName);
			view.setTabIcon(icon);

			return view;
		}

		private static View createMainView() {
			JTabbedPane tabbedPane = new JTabbedPane();
			tabbedPane.addTab("Sample1", new JTextArea("Sample1"));
			tabbedPane.addTab("Sample2", new JTextArea("Sample2"));
			tabbedPane.addTab("Sample3", new JTextArea("Sample3"));

			View mainView = new View(MAIN_VIEW, "");

			//blokujemy mozliwosc dokowania do tego view w regionie CENTER
			mainView.setTerritoryBlocked(Region.CENTER, true);

			//ustawiamy komponent GUI, ktory chcemy aby byl wyswietalny w tym view
			mainView.setContentPane(new JScrollPane(tabbedPane));

			return mainView;
		}
	}

}
