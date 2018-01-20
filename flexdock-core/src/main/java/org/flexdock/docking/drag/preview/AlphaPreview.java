/* Copyright (c) 2004 Ismail Degani, Christopher M Butler
Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in the
Software without restriction, including without limitation the rights to use,
copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
Software, and to permit persons to whom the Software is furnished to do so, subject
to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package org.flexdock.docking.drag.preview;

import org.flexdock.docking.Dockable;
import org.flexdock.docking.drag.effects.DefaultPreview;

import java.awt.*;
import java.util.Map;

public class AlphaPreview extends DefaultPreview {
	
	private static final Color DEFAULT_COLOR = Color.WHITE;
	private static final Color DEFAULT_BORDER = Color.BLACK;
	
	private float previewAlpha;
	private Color previewColor;
	private Color borderColor;
	
	// TODO Currently used when reflecting to create an instance for EffectsManager defaults
	public AlphaPreview() {
		this(Color.black, new Color(119, 173, 255), 0.25f);
	}
	
	public AlphaPreview(Color border, Color fill, float alpha) {
		setBorderColor(border);
		setPreviewColor(fill);
		setAlpha(alpha);
	}
	
	private void setPreviewColor(Color color) {
		previewColor = color == null ? DEFAULT_COLOR : color;
	}
	
	private void setAlpha(float alpha) {
		alpha = Math.max(0, alpha);
		alpha = Math.min(alpha, 1.0f);
		previewAlpha = alpha;
	}
	
	private void setBorderColor(Color color) {
		borderColor = color == null ? DEFAULT_BORDER : color;
	}
	
	@Override
	public void drawPreview(Graphics2D g, Polygon p, Dockable dockable, Map dragInfo) {
		Rectangle rect = p.getBounds();
		
		g.setColor(borderColor);
		g.draw3DRect(rect.x, rect.y, rect.width - 1, rect.height - 1, false);
		Composite composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, previewAlpha);
		g.setComposite(composite);
		g.setColor(previewColor);
		g.fillRect(rect.x, rect.y, rect.width, rect.height);
	}
	
}
