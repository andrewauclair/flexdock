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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author Christopher Butler
 */
public class Titlebar extends JPanel {
	private JLabel titleLabel = new JLabel();

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
		JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 2, 0));
		actionPanel.setBorder(null);
		add(actionPanel, gbc);

		// TODO Maximize seems to be broken. When undoing it the mainframe doesn't repaint
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					DockingManager.toggleMaximized(view);
				}
			}
		});

		Color bgColor = new Color(183, 201, 217);

		setBorder(BorderFactory.createMatteBorder(1, 1, 0, 1, Color.GRAY));
		actionPanel.setBackground(bgColor);
		setBackground(bgColor);

		gbc.anchor = GridBagConstraints.NORTHEAST;
		gbc.weightx = 1.0;


//		closeButton.setContentAreaFilled(false);
//		closeButton.setBorder(null);
//		closeButton.setBorderPainted(false);
//
//		closeButton.setFocusable(false);
//		closeButton.setFocusPainted(false);
//
//		closeButton.setRolloverEnabled(true);
//
//		closeButton.setMargin(new Insets(0, 0, 0, 0));

		closeButton.addActionListener(e -> DockingManager.close(view));
		closeButton.setIcon(new ImageIcon(getClass().getResource("/org/flexdock/plaf/titlebar/win32/close_default.png")));
		pinButton.addActionListener(e -> {
			if (view.isMinimized()) {
				pinButton.setIcon(new ImageIcon(getClass().getResource("/org/flexdock/plaf/titlebar/win32/pin_default.png")));
			}
			else {
				pinButton.setIcon(new ImageIcon(getClass().getResource("/org/flexdock/plaf/titlebar/win32/pin_default_selected.png")));
			}
			DockingManager.setMinimized(view, !view.isMinimized());
		});
		pinButton.setIcon(new ImageIcon(getClass().getResource("/org/flexdock/plaf/titlebar/win32/pin_default.png")));

//		closeButton.setBorder(null);
//		pinButton.setBorder(null);

		pinButton.setBackground(bgColor);
		pinButton.setContentAreaFilled(false);
		closeButton.setContentAreaFilled(false);

		pinButton.getModel().addChangeListener(e -> {
			ButtonModel model = (ButtonModel) e.getSource();
			if (model.isRollover()) {
				pinButton.setContentAreaFilled(true);
			}
			else {
				pinButton.setContentAreaFilled(false);
			}
		});

		closeButton.getModel().addChangeListener(e -> {
			ButtonModel model = (ButtonModel) e.getSource();
			if (model.isRollover()) {
				closeButton.setContentAreaFilled(true);
			}
			else {
				closeButton.setContentAreaFilled(false);
			}
		});

		pinButton.setPreferredSize(new Dimension(20, 20));
		closeButton.setPreferredSize(new Dimension(20, 20));

		actionPanel.add(pinButton);
		actionPanel.add(closeButton);
	}

	// title border 0, 4, 0, 0
	// close button border 4, 4, 4, 4
	// title color 183, 201, 217

	public String getText() {
		return titleLabel.getText();
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
