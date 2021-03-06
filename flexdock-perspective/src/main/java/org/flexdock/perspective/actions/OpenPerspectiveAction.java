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
package org.flexdock.perspective.actions;

import com.sun.istack.internal.NotNull;
import org.flexdock.perspective.Perspective;
import org.flexdock.perspective.PerspectiveManager;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Objects;

/**
 * @author Christopher Butler
 */
public class OpenPerspectiveAction extends AbstractAction {
	private String perspective;

	public OpenPerspectiveAction(@NotNull String perspectiveId) {
		Objects.requireNonNull(perspectiveId);
		this.perspective = perspectiveId;

		Perspective perspective = getPerspective();
		if (perspective != null) {
			putValue(Action.NAME, perspective.getName());
		}
	}

	public Perspective getPerspective() {
		return PerspectiveManager.getInstance().getPerspective(this.perspective);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (this.perspective != null) {
			PerspectiveManager.getInstance().loadPerspective(this.perspective);
		}
	}
}
