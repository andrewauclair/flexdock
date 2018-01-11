package org.flexdock.test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class FloatDragTest {

    private static boolean create;
    private static JFrame popup;

    public static void main(String[] args) {

        JFrame mainFrame = new JFrame("Main Frame");

        JPanel panel = new JPanel();
        panel.setBackground(Color.blue);

        mainFrame.add(panel, BorderLayout.CENTER);

        mainFrame.setVisible(true);

        create = true;

        panel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (create) {
                    create = false;


                    try {
                        Robot robot = new Robot();
                        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                    } catch (AWTException e1) {
                        e1.printStackTrace();
                    }
                    SwingUtilities.invokeLater(() -> {
                        try {
                            popup = new JFrame("Popup");
                            mainFrame.remove(panel);
                            popup.add(panel, BorderLayout.CENTER);
                            popup.setSize(200, 200);
                            popup.setVisible(true);

                            Robot robot = new Robot();
                            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                            robot.mouseMove(e.getX(), e.getY());
                        } catch (AWTException e1) {
                            e1.printStackTrace();
                        }
                    });

                } else {
                    System.out.println(this + " " + e.getPoint());
                    popup.setLocation(e.getX() - popup.getWidth(), e.getY() - popup.getHeight());
                }
            }
        });
    }
}
