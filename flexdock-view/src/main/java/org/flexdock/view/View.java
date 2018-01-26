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
package org.flexdock.view;

import org.flexdock.docking.*;
import org.flexdock.docking.defaults.DefaultDockingStrategy;
import org.flexdock.docking.event.DockingEvent;
import org.flexdock.docking.event.DockingListener;
import org.flexdock.docking.props.DockablePropertySet;
import org.flexdock.docking.props.PropertyManager;
import org.flexdock.plaf.PlafManager;
import org.flexdock.util.DockingUtility;
import org.flexdock.util.SwingUtility;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The {@code View} class is slightly incompatible with {@code JComponent}.
 * Similar to JFC/Swing top-level containers, a {@code View} contains only a
 * content pane {@code Container} and a {@code Titlebar}. The <b>content pane</b>
 * should contain all the components displayed by the {@code View}. As a
 * convenience {@code add} and its variants, {@code remove(Component)} and
 * {@code setLayout} have been overridden to forward to the
 * {@code contentPane} as necessary. This means you can write:
 * <p>
 * <pre>
 * view.add(child);
 * </pre>
 * <p>
 * And the child will be added to the contentPane. The content pane will always
 * be non-null. Attempting to set it to null will cause the View to throw an
 * exception. The default content pane will not have a layout manager set.
 *
 * @author Christopher Butler
 * @author Karl Schaefer
 * @see javax.swing.JFrame
 * @see javax.swing.JRootPane
 */
public class View extends JComponent implements Dockable {
	private static final String UI_CLASS_ID = "Flexdock.view";

	private static final String ACTION_TOGGLE_NEXT = "toggleNextView";
	private static final String ACTION_TOGGLE_PREVIOUS = "togglePreviousView";

	static final DockingStrategy VIEW_DOCKING_STRATEGY = createDockingStrategy();

	private String persistentId;

	private Titlebar titlepane;

	private Container contentPane;

	private boolean contentPaneCheckingEnabled;

	private ArrayList<DockingListener> dockingListeners;

	private ArrayList<Component> dragSources;

	private HashSet<Component> frameDragSources;

	private transient HashSet<String> blockedActions;

	static {
		DockingManager.setDockingStrategy(View.class, VIEW_DOCKING_STRATEGY);
		PropertyManager.setDockablePropertyType(View.class, ViewProps.class);
	}

	public View(String persistentId) {
		this(persistentId, "");
	}

	public View(String persistentId, String title) {
		this(persistentId, title, title);
	}

	private View(String persistentId, String title, String tabText) {
		if (persistentId == null) {
			throw new IllegalArgumentException(
					"The 'persistentId' parameter cannot be null.");
		}

		this.persistentId = persistentId;

		dragSources = new ArrayList<>(1);
		frameDragSources = new HashSet<>(1);
		dockingListeners = new ArrayList<>(1);

		setContentPane(createContentPane());
		if (!title.isEmpty()) {
			setTitlebar(new Titlebar(this, title));
		}
		setLayout(createLayout());
		this.contentPaneCheckingEnabled = true;

		if (tabText == null) {
			tabText = title;
		}
		setTabText(tabText);

		updateUI();

		DockingManager.registerDockable(this);

		getActionMap().put(ACTION_TOGGLE_NEXT, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtility.toggleFocus(+1);
			}
		});
		getActionMap().put(ACTION_TOGGLE_PREVIOUS, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtility.toggleFocus(-1);
			}
		});
	}

	private static DockingStrategy createDockingStrategy() {
		return new DefaultDockingStrategy() {
			@Override
			protected DockingPort createDockingPortImpl(DockingPort base) {
				return new Viewport();
			}
		};
	}

	public static View getInstance(String viewId) {
		Dockable view = DockingManager.getDockable(viewId);
		return view instanceof View ? (View) view : null;
	}

	protected Container createContentPane() {
		return new JPanel();
	}

	private LayoutManager createLayout() {
		return new GridBagLayout();
	}

	private Container getContentPane() {
		return contentPane;
	}

	public Titlebar getTitlebar() {
		return titlepane;
	}

	@Override
	public DockablePropertySet getDockingProperties() {
		return PropertyManager.getDockablePropertySet(this);
	}

	private ViewProps getViewProperties() {
		return (ViewProps) getDockingProperties();
	}

	/**
	 * Sets the content pane for this view.
	 *
	 * @param container the container to use as the content pane.
	 * @throws IllegalArgumentException if {@code container} is {@code null} or if {@code container} is the
	 *                                  {@code titlepane}.
	 * @see #titlepane
	 */
	public void setContentPane(Container container) throws IllegalArgumentException {
		if (container == null) {
			throw new IllegalArgumentException(
					"Unable to set a null content pane.");
		}
		if (container == titlepane) {
			throw new IllegalArgumentException(
					"Cannot use the same container as both content pane and titlebar.");
		}

		if (contentPane != null) {
			remove(contentPane);
		}
		contentPane = container;
		boolean checkingEnabled = contentPaneCheckingEnabled;
		try {
			this.contentPaneCheckingEnabled = false;
			GridBagConstraints gbc = new GridBagConstraints();

			gbc.weightx = 1;
			gbc.weighty = 0;
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.fill = GridBagConstraints.HORIZONTAL;

			if (titlepane != null) {
				add(titlepane, gbc);
			}

			gbc.weightx = 1;
			gbc.weighty = 1;
			gbc.gridx = 0;
			gbc.gridy = titlepane == null ? 0 : 1;
			gbc.fill = GridBagConstraints.BOTH;

			add(contentPane, gbc);
		}
		finally {
			this.contentPaneCheckingEnabled = checkingEnabled;
		}
	}

	private void setTitlebar(Titlebar titlebar) {
		titlepane = titlebar;

		boolean checkingEnabled = contentPaneCheckingEnabled;
		try {
			this.contentPaneCheckingEnabled = false;

			GridBagConstraints gbc = new GridBagConstraints();

			gbc.weightx = 1;
			gbc.weighty = 0;
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.fill = GridBagConstraints.HORIZONTAL;

			if (titlepane != null) {
				add(titlepane, gbc);
			}

			gbc.weightx = 1;
			gbc.weighty = 1;
			gbc.gridx = 0;
			gbc.gridy = titlepane == null ? 0 : 1;
			gbc.fill = GridBagConstraints.BOTH;

			add(contentPane, gbc);
		}
		finally {
			this.contentPaneCheckingEnabled = checkingEnabled;
		}

		dragSources.add(titlepane);
		frameDragSources.add(titlepane);
		DockingManager.updateDragListeners(this);
	}

	public String getTitle() {
		if (titlepane != null) {
			return titlepane.getText();
		}
		return "";
	}

	@Override
	public void updateUI() {
		setUI(PlafManager.getUI(this));
	}

	@Override
	public String getUIClassID() {
		return UI_CLASS_ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void addImpl(Component comp, Object constraints, int index) {
		if (contentPaneCheckingEnabled) {
			contentPane.add(comp, constraints, index);
		}
		else {
			super.addImpl(comp, constraints, index);
		}
	}

	@Override
	public void remove(Component comp) {
		if (comp == contentPane) {
			super.remove(comp);
		}
		else {
			contentPane.remove(comp);
		}
	}

	@Override
	public Component getComponent() {
		return this;
	}

	@Override
	public List<Component> getDragSources() {
		return dragSources;
	}

	@Override
	public Set<Component> getFrameDragSources() {
		return frameDragSources;
	}

	@Override
	public String getPersistentId() {
		return persistentId;
	}

	public boolean isTerritoryBlocked(Dockable dockable, DockingConstants.Region region) {
		return getDockingProperties().isTerritoryBlocked(region);
	}

	public void setTerritoryBlocked(DockingConstants.Region region, boolean blocked) {
		getDockingProperties().setTerritoryBlocked(region, blocked);
	}

	public String getTabText() {
		String txt = getDockingProperties().getDockableDesc();
		return txt == null ? getTitle() : txt;
	}

	private void setTabText(String tabText) {
		getDockingProperties().setDockableDesc(tabText);
	}

	public Icon getTabIcon() {
		return getDockingProperties().getTabIcon();
	}

	public void setTabIcon(Icon icon) {
		getDockingProperties().setTabIcon(icon);
	}

	@Override
	public boolean dock(Dockable dockable) {
		return dock(dockable, DockingConstants.Region.CENTER);
	}

	@Override
	public DockingPort getDockingPort() {
		return DockingManager.getDockingPort((Dockable) this);
	}

	public Dockable getSibling(DockingConstants.Region region) {
		return DefaultDockingStrategy.getSibling(this, region);
	}

	public Viewport getViewport() {
		DockingPort port = getDockingPort();
		return port instanceof Viewport ? (Viewport) port : null;
	}

	@Override
	public boolean dock(Dockable dockable, DockingConstants.Region relativeRegion) {
		return DockingManager.dock(dockable, this, relativeRegion);
	}

	@Override
	public boolean dock(Dockable dockable, DockingConstants.Region relativeRegion, float ratio) {
		return DockingManager.dock(dockable, this, relativeRegion, ratio);
	}

	public void setActive(boolean b) {
		getViewProperties().setActive(b);
	}

	public boolean isActive() {
		return getViewProperties().isActive();
	}

	public void setActiveStateLocked(boolean b) {
		getViewProperties().setActiveStateLocked(b);
	}

	public boolean isActiveStateLocked() {
		return getViewProperties().isActiveStateLocked();
	}

	public boolean isMinimized() {
		return DockingUtility.isMinimized(this);
	}

	public int getMinimizedConstraint() {
		return DockingUtility.getMinimizedConstraint(this);
	}

	@Override
	public void addDockingListener(DockingListener listener) {
		dockingListeners.add(listener);
	}

	@Override
	public DockingListener[] getDockingListeners() {
		return dockingListeners.toArray(new DockingListener[dockingListeners.size()]);
	}

	@Override
	public void removeDockingListener(DockingListener listener) {
		dockingListeners.remove(listener);
	}

	@Override
	public void dockingCanceled(DockingEvent evt) {
	}

	@Override
	public void dockingComplete(DockingEvent evt) {
		setActionBlocked(DockingConstants.PIN_ACTION, isFloating());
		if (titlepane != null) {
			titlepane.revalidate();
		}
	}

	@Override
	public void dragStarted(DockingEvent evt) {
	}

	@Override
	public void dropStarted(DockingEvent evt) {
	}

	@Override
	public void undockingComplete(DockingEvent evt) {
	}

	@Override
	public void undockingStarted(DockingEvent evt) {
	}

	private void setActionBlocked(String actionName, boolean blocked) {
		if (actionName == null) {
			return;
		}

		Set<String> actions = getBlockedActions();
		if (blocked) {
			actions.add(actionName);
		}
		else {
			if (actions != null) {
				actions.remove(actionName);
			}
		}
	}

	public boolean isActionBlocked(String actionName) {
		return actionName != null && blockedActions != null && blockedActions.contains(actionName);
	}

	private HashSet<String> getBlockedActions() {
		if (blockedActions == null) {
			blockedActions = new HashSet<>(1);
		}
		return blockedActions;
	}

	private boolean isFloating() {
		return DockingUtility.isFloating(this);
	}

	@Override
	protected String paramString() {
		return "id=" + persistentId + "," + super.paramString();
	}

	/**
	 * Sets the <code>LayoutManager</code>. Overridden to conditionally
	 * forward the call to the <code>contentPane</code>.
	 *
	 * @param manager the <code>LayoutManager</code>
	 */
	@Override
	public void setLayout(LayoutManager manager) {
		if (contentPaneCheckingEnabled) {
			contentPane.setLayout(manager);
		}
		else {
			super.setLayout(manager);
		}
	}
}
