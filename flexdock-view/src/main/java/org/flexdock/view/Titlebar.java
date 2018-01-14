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

import org.flexdock.view.actions.DefaultCloseAction;
import org.flexdock.view.actions.DefaultPinAction;
import org.flexdock.view.model.ViewButtonModel;

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

	private View parentView;
	private JLabel titleLabel;

	public Titlebar() {
		this(null, null);
	}

	public Titlebar(String title) {
		this(title, null);
	}

	public Titlebar(Action[] actions) {
		this(null, actions);
	}

	// title border 0, 4, 0, 0
	// close button border 4, 4, 4, 4
	// title color 183, 201, 217

	public Titlebar(String title, Action[] actions) {
		super(new FlowLayout());
		titleLabel = new JLabel();
		add(titleLabel);
		setText(title);
		setActions(actions);
	}

	/**
	 * Sets the text for this titlebar to {@code text} or empty string if text
	 * is {@code null}.
	 *
	 * @param text the text to set.
	 */
	public void setText(String text) {
		titleText = text == null ? "" : text;
		titleLabel.setText(titleText);
	}

	protected void setActions(Action[] actions) {
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

	public synchronized void addAction(String actionName) {
		if (actionName.equals("close")) {
			JButton button = new JButton(new DefaultCloseAction());
			button.setIcon(new ImageIcon(getClass().getResource("/org/flexdock/plaf/titlebar/win32/close_default.png")));
			add(button);
		}
		else if (actionName.equals("pin")) {
			JButton button = new JButton(new DefaultPinAction());
			button.setIcon(new ImageIcon(getClass().getResource("/org/flexdock/plaf/titlebar/win32/pin_default.png")));
			add(button);
		}
	}

	public synchronized void addAction(Action action) {
		JButton button = new JButton(action);

		add(button);
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
		remove(button);
		actionButtons.remove(key);
		// remove the action
		Action action = getAction(key);
		actionList.remove(action);
		regenerateButtonList();
		updateButtonModels();
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
		return parentView != null && parentView.isActive();
	}

	void setView(View view) {
		setParentView(view);
	}

	private void setParentView(View view) {
		parentView = view;
		updateButtonModels();
	}

	private void updateButtonModels() {
		String viewId = parentView == null ? null : parentView
				.getPersistentId();
		Component[] comps = getComponents();
		for (Component comp : comps) {
			Button button = comp instanceof Button ? (Button) comp
					: null;
			if (button == null) {
				continue;
			}

			ButtonModel bm = button.getModel();
			if (bm instanceof ViewButtonModel) {
				((ViewButtonModel) bm).setViewId(viewId);
			}
		}
	}

	public View getView() {
		return (View) SwingUtilities.getAncestorOfClass(View.class, this);
	}
}
