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
package org.flexdock.docking.floating.frames;

import org.flexdock.util.SwingUtility;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * @author Christopher Butler
 */
public class FrameDragListener implements MouseListener, MouseMotionListener {
	private Point dragOffset;
	private DockingFrame frame;
	private boolean enabled;
	
	FrameDragListener(DockingFrame frame) {
		this.frame = frame;
	}

	@Override
	public void mouseMoved(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
//		System.out.println("mousePressed");
		dragOffset = e.getPoint();
		Component c = (Component) e.getSource();
		if (c != frame) {
			dragOffset = SwingUtilities.convertPoint(c, dragOffset, frame);
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
//		System.out.println("Mouse dragged: " + enabled);
		if (enabled) {
			Point loc = e.getPoint();
			SwingUtilities.convertPointToScreen(loc, (Component) e.getSource());
			SwingUtility.subtract(loc, dragOffset);
			frame.setLocation(loc);
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
//		System.out.println("mouseClicked");
	}

	@Override
	public void mouseEntered(MouseEvent e) {
//		System.out.println("mouseEntered");
	}

	@Override
	public void mouseExited(MouseEvent e) {
//		System.out.println("mouseExited");
	}

	@Override
	public void mouseReleased(MouseEvent e) {
//		System.out.println("mouseReleased");
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
//		System.out.println("set enabled: " + enabled);
		this.enabled = enabled;
	}
}
