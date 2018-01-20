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

import org.flexdock.docking.DockingManager;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Christopher Butler
 */
public class Titlebar extends JPanel {

	private Icon titleIcon;

	private String titleText;

	private List<Action> actionList;

	private HashMap<String, Button> actionButtons;

	private JLabel titleLabel = new JLabel();

	private JPanel actionPanel = new JPanel(new FlowLayout());

	private View view;

	private JButton closeButton = new JButton();
	private JButton pinButton = new JButton();

	Titlebar(View view, String title) {
		super(new GridBagLayout());
		this.view = view;

		titleLabel.setText(title);
		titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.weightx = 1.0;
		add(titleLabel, gbc);

		gbc.anchor = GridBagConstraints.EAST;
		add(actionPanel, gbc);

		setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		actionPanel.setBackground(new Color(119, 173, 255));

		gbc.anchor = GridBagConstraints.NORTHEAST;
		gbc.weightx = 1.0;


//		closeButton.setContentAreaFilled(false);
//		closeButton.setBorder(null);
		closeButton.setBorderPainted(false);

		closeButton.setFocusable(false);
		closeButton.setFocusPainted(false);

		closeButton.setRolloverEnabled(true);

		closeButton.setMargin(new Insets(0, 0, 0, 0));

		closeButton.addActionListener(e -> DockingManager.close(view));
		closeButton.setIcon(new ImageIcon(getClass().getResource("/org/flexdock/plaf/titlebar/win32/close_default.png")));
		pinButton.addActionListener(e -> DockingManager.setMinimized(view, !view.isMinimized()));
		pinButton.setIcon(new ImageIcon(getClass().getResource("/org/flexdock/plaf/titlebar/win32/pin_default.png")));

		actionPanel.add(pinButton, gbc);
		actionPanel.add(closeButton, gbc);
	}

	// title border 0, 4, 0, 0
	// close button border 4, 4, 4, 4
	// title color 183, 201, 217

	/**
	 * Sets the text for this titlebar to {@code text}.
	 *
	 * @param text the text to set.
	 */
	private void setText(String text) {
		titleLabel.setText(text);
	}

	private void setActions(Action[] actions) {
		if (actions == null) {
			actions = new Action[0];
			actionList = new ArrayList<>(3);
			actionButtons = new HashMap<>(3);
		}

		removeAllActions();
		for (Action action : actions) {
			addAction(action);
		}
	}

	public synchronized void addAction(String actionName, View view) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHEAST;
		gbc.weightx = 1.0;

		JButton button = new JButton();
//		button.setContentAreaFilled(false);
//		button.setBorder(null);
		button.setBorderPainted(false);

		button.setFocusable(false);
		button.setFocusPainted(false);

		button.setRolloverEnabled(true);

		button.setMargin(new Insets(0, 0, 0, 0));

		if (actionName.equals("close")) {
			button.addActionListener(e -> DockingManager.close(view));
			button.setIcon(new ImageIcon(getClass().getResource("/org/flexdock/plaf/titlebar/win32/close_default.png")));
		}
		else if (actionName.equals("pin")) {
			button.addActionListener(e -> DockingManager.setMinimized(view, !view.isMinimized()));
			button.setIcon(new ImageIcon(getClass().getResource("/org/flexdock/plaf/titlebar/win32/pin_default.png")));
		}

		actionPanel.add(button, gbc);
	}

	public synchronized void addAction(Action action) {
		add(new JButton(action));
	}

	private void regenerateButtonList() {
		Button[] list = new Button[actionList.size()];
		for (int i = 0; i < list.length; i++) {
			Action action = actionList.get(i);
			String key = getKey(action);
			list[i] = getButton(key);
		}

		synchronized (this) {
			Button[] buttonList = list;
		}
	}

	private Action getAction(String key) {
		if (key == null) {
			return null;
		}

		for (Object anActionList : actionList) {
			Action action = (Action) anActionList;
			String actionName = (String) action.getValue(Action.NAME);
			if (key.equals(actionName)) {
				return action;
			}
		}
		return null;
	}

	public Action[] getActions() {
		return actionList.toArray(new Action[0]);
	}

	private Button getButton(String key) {
		return actionButtons.get(key);
	}

	public AbstractButton getActionButton(String actionName) {
		return getButton(actionName);
	}

	private boolean hasAction(String key) {
		return actionButtons.containsKey(key);
	}

	public Icon getIcon() {
		return titleIcon;
	}

	public String getText() {
		return titleText;
	}

	public void removeAction(Action action) {
		if (action == null) {
			return;
		}

		String key = getKey(action);
		removeAction(key);
	}

	private synchronized void removeAction(String key) {
		if (!hasAction(key)) {
			return;
		}

		// Remove button associated with this action.
		Button button = getButton(key);
		actionPanel.remove(button);
		actionButtons.remove(key);
		// remove the action
		Action action = getAction(key);
		actionList.remove(action);
		regenerateButtonList();
	}

	protected synchronized void removeAllActions() {
		if (actionList == null) {
			return;
		}

		while (actionList.size() > 0) {
			Action action = actionList.get(0);
			String key = getKey(action);
			// Remove button associated with this action.
			Button button = getButton(key);
			remove(button);
			actionButtons.remove(key);
			// remove the action
			actionList.remove(0);
		}
		regenerateButtonList();
	}

	private String getKey(Action action) {
		Object obj = action == null ? null : action.getValue(Action.NAME);
		return obj instanceof String ? (String) obj : null;
	}

	protected Icon getIcon(Action action) {
		Object obj = action == null ? null : action.getValue(Action.SMALL_ICON);
		return obj instanceof Icon ? (Icon) obj : null;
	}

	public void setIcon(Icon icon) {
		titleIcon = icon;
	}

	public boolean isActive() {
		return view.isActive();
	}

	public View getView() {
		return view;
	}

	public void showClose() {
		closeButton.setVisible(true);
	}

	public void hideClose() {
		closeButton.setVisible(false);
	}

	public void showPin() {
		pinButton.setVisible(true);
	}

	public void hidePin() {
		pinButton.setVisible(false);
	}
}
