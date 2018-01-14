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
package org.flexdock.plaf.theme;

import org.flexdock.plaf.FlexViewComponentUI;
import org.flexdock.plaf.icons.IconMap;
import org.flexdock.plaf.icons.IconResource;
import org.flexdock.plaf.icons.IconResourceFactory;
import org.flexdock.plaf.resources.paint.Painter;
import org.flexdock.view.Button;
import org.flexdock.view.Titlebar;
import org.flexdock.view.View;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.UIResource;
import java.awt.*;

/**
 * @deprecated Removing TitlebarUI classes
 * @author Christopher Butler
 */
public class TitlebarUI extends FlexViewComponentUI {

	private static final String DEFAULT_HEIGHT = "default.height";
	private static final String FONT = "font";
	private static final String FONT_COLOR = "font.color";
	private static final String FONT_COLOR_ACTIVE = "font.color.active";
	private static final String BACKGROUND_COLOR = "bgcolor";
	private static final String BACKGROUND_COLOR_ACTIVE = "bgcolor.active";
	private static final String BORDER = "border";
	private static final String BORDER_ACTIVE = "border.active";
	private static final String PAINTER = "painter";
	private static final String INSETS = "insets";
    private static final String BUTTON_MARGIN = "button.margin";
	private static final String ICON_INSETS = "icon.insets";
	private static final String ANTIALIASING = "antialiasing";
	private static final int MINIMUM_HEIGHT = 12;

	private Font font;
	private Color activeFont;
	private Color inactiveFont;
	private Color activeBackground;
	private Color inactiveBackground;
	private Border activeBorder;
	private Border inactiveBorder;
	private IconMap defaultIcons;
    protected Painter painter;
    protected Insets insets;
	private int buttonMargin;
	private Insets iconInsets;
	private Object antialiasing;
	private int defaultHeight = MINIMUM_HEIGHT;


    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        Dimension d = c.getPreferredSize();
        d.height = getDefaultHeight();
        c.setPreferredSize(d);

        reconfigureActions(c);

        if (font != null) {
            Font current = c.getFont();

            if (current == null || current instanceof UIResource) {
                c.setFont(new FontUIResource(font));
            }
        }
    }

    @Override
    public void uninstallUI(JComponent c) {
        super.uninstallUI(c);
    }

    @Override
    public void paint(Graphics g, JComponent jc) {
        Titlebar titlebar = (Titlebar) jc;
        paintBackground(g, titlebar);
        paintIcon(g, titlebar);
        paintTitle(g, titlebar);
        paintBorder(g, titlebar);
    }

    protected void paintBackground(Graphics g, Titlebar titlebar) {
        Rectangle paintArea = getPaintRect(titlebar);
        g.translate(paintArea.x, paintArea.y);
        painter.paint(g, paintArea.width, paintArea.height, titlebar.isActive(), titlebar);
        g.translate(-paintArea.x, -paintArea.y);
    }

    protected Rectangle getPaintRect(Titlebar titlebar) {
        if (getInsets() == null) {
            return new Rectangle(0, 0, titlebar.getWidth(), titlebar.getHeight());
        }

        Insets paintInsets = getInsets();
        return new Rectangle(paintInsets.left, paintInsets.top,
                             (titlebar.getWidth() - paintInsets.right - paintInsets.left), (titlebar.getHeight()
                                     - paintInsets.bottom - paintInsets.top));
    }

	private void paintTitle(Graphics g, Titlebar titlebar) {
        if (titlebar.getText() == null) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        Object oldAAValue = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antialiasing);

        g2.setFont(titlebar.getFont());
        Rectangle iconRect = getIconRect(titlebar);
        Rectangle paintRect = getPaintRect(titlebar);

        int x = getTextLocation(iconRect);

        //Center text vertically.
        FontMetrics fm = g.getFontMetrics();
        int y = (paintRect.height + fm.getAscent() - fm.getLeading() -
                 fm.getDescent()) / 2;

        Color c = getFontColor(titlebar.isActive());
        g2.setColor(c);
        g.translate(paintRect.x, paintRect.y);
        g2.drawString(titlebar.getText(), x, y);
        g.translate(-paintRect.x, -paintRect.y);


        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAAValue);
    }

    protected int getTextLocation(Rectangle iconRect) {
        if (iconRect.width > 0) {
            return iconRect.x + iconRect.width + getRightIconMargin();
        }
        return 5;
    }

	private void paintIcon(Graphics g, Titlebar titlebar) {
        if (titlebar.getIcon() == null) {
            return;
        }

        Icon icon = titlebar.getIcon();
        Rectangle r = getIconRect(titlebar);

        Rectangle paintRect = getPaintRect(titlebar);
        g.translate(paintRect.x, paintRect.y);
        icon.paintIcon(titlebar, g, r.x, r.y);
        g.translate(-paintRect.x, -paintRect.y);
    }

    protected Rectangle getIconRect(Titlebar titlebar) {
        Icon icon = titlebar.getIcon();
        Rectangle r = new Rectangle(0, 0, 0, 0);
        if (icon == null) {
            return r;
        }

        Rectangle paintRect = getPaintRect(titlebar);

        r.x = getLeftIconMargin();
        r.width = icon.getIconWidth();
        r.height = icon.getIconHeight();
        r.y = (paintRect.height - r.height) / 2;
        return r;
    }

	private int getLeftIconMargin() {
        return getIconInsets() == null ? 2 : getIconInsets().right;
    }

	private int getRightIconMargin() {
        return getIconInsets() == null ? 2 : getIconInsets().right;
    }

    protected void paintBorder(Graphics g, Titlebar titlebar) {
        Border border = getBorder(titlebar);
        if (border != null) {
            Rectangle rectangle = getPaintRect(titlebar);
            g.translate(rectangle.x, rectangle.y);
            border.paintBorder(titlebar, g, 0, 0, rectangle.width, rectangle.height);
            g.translate(-rectangle.x, -rectangle.y);
        }
    }

    public void layoutComponents(Titlebar titlebar) {
        Rectangle rectangle = getPaintRect(titlebar);
        int margin = getButtonMargin();
        int h = rectangle.height - 2 * margin;
        int x = rectangle.width - margin - h;

        View view = titlebar.getView();
        Component[] c = titlebar.getComponents();
		for (Component aC : c) {
			// start out with the preferred width
			int width = aC.getPreferredSize().width;
			if (aC instanceof Button) {
				// org.flexdock.view.Buttons will be rendered as squares
				width = h;
				// don't show the button if its corresponding action is blocked
				if (view != null && view.isActionBlocked(((Button) aC).getActionName())) {
					aC.setBounds(0, 0, 0, 0);
					continue;
				}
			}
			// layout the component over to the right
			aC.setBounds(x, margin + rectangle.y, width, h);
			// move x to the left for the next component
			x -= width;
		}
    }

    public void configureAction(Action action) {
        if (action == null) {
            return;
        }

        IconResource icons = getIcons(action);
        if (icons != null) {
            action.putValue(ICON_RESOURCE, icons);
        }
    }

    private void reconfigureActions(JComponent c) {
        Component[] c1 = c.getComponents();
		for (Component aC1 : c1) {
			if (!(aC1 instanceof Button)) {
				continue;
			}
			Button b = (Button) aC1;
			configureAction(b.getAction());
		}
    }

	private Color getFontColor(boolean active) {
        Color c = active ? activeFont : inactiveFont;
        return c == null ? inactiveFont : c;
    }

    protected Color getBackgroundColor(boolean active) {
        Color color = active ? activeBackground : inactiveBackground;
        return color == null ? inactiveBackground : color;
    }

	private Border getBorder(Titlebar titlebar) {
        boolean active = titlebar.isActive();
        return active ? activeBorder : inactiveBorder;
    }

    public int getDefaultHeight() {
        return defaultHeight;
    }

	private void setDefaultHeight(int defaultHeight) {
        defaultHeight = Math.max(defaultHeight, MINIMUM_HEIGHT);
        this.defaultHeight = defaultHeight;
    }

    public Dimension getPreferredSize() {
        return new Dimension(10, getDefaultHeight());
    }

    /**
     * @return Returns the activeBackground.
     */
    public Color getActiveBackground() {
        return activeBackground;
    }

    /**
     * @param activeBackground
     *            The activeBackground to set.
     */
	private void setActiveBackground(Color activeBackground) {
        this.activeBackground = activeBackground;
    }

    /**
     * @return Returns the activeFont.
     */
    public Color getActiveFont() {
        return activeFont;
    }

    /**
     * @param activeFont
     *            The activeFont to set.
     */
	private void setActiveFont(Color activeFont) {
        this.activeFont = activeFont;
    }

    /**
     * @return Returns the inactiveBackground.
     */
    public Color getInactiveBackground() {
        return inactiveBackground;
    }

    /**
     * @param inactiveBackground
     *            The inactiveBackground to set.
     */
	private void setInactiveBackground(Color inactiveBackground) {
        this.inactiveBackground = inactiveBackground;
    }

    /**
     * @return Returns the inactiveFont.
     */
    public Color getInactiveFont() {
        return inactiveFont;
    }

    /**
     * @param inactiveFont
     *            The inactiveFont to set.
     */
	private void setInactiveFont(Color inactiveFont) {
        this.inactiveFont = inactiveFont;
    }

    /**
     * @return Returns the font.
     */
	protected Font getFont() {
        return font;
    }

    /**
     * @param font
     *            The font to set.
     */
	private void setFont(Font font) {
        this.font = font;
    }

    public IconMap getDefaultIcons() {
        return defaultIcons;
    }

	private void setDefaultIcons(IconMap defaultIcons) {
		this.defaultIcons = defaultIcons;
    }

	private void setDefaultIcons(String iconMapName) {
        IconMap map = IconResourceFactory.getIconMap(iconMapName);
        setDefaultIcons(map);
    }

	private IconResource getIcons(Action action) {
        String key = action == null ? null : (String) action.getValue(Action.NAME);
        return getIcons(key);
    }

	private IconResource getIcons(String key) {
        return defaultIcons == null ? null : defaultIcons.getIcons(key);
    }

	public Action getAction(String actionKey) {
        IconResource resource = getIcons(actionKey);
        Action action = resource==null? null: resource.getAction();
        if(action!=null) {
            action.putValue(Action.NAME, actionKey);
        }
        return action;
    }

    /**
     * @return Returns the inactiveBorder.
     */
    public Border getInactiveBorder() {
        return inactiveBorder;
    }

    /**
     * @param inactiveBorder
     *            The inactiveBorder to set.
     */
	private void setInactiveBorder(Border inactiveBorder) {
        this.inactiveBorder = inactiveBorder;
    }

    /**
     * @return Returns the activeBorder.
     */
    public Border getActiveBorder() {
        return activeBorder;
    }

    /**
     * @param activeBorder
     *            The activeBorder to set.
     */
	private void setActiveBorder(Border activeBorder) {
        this.activeBorder = activeBorder;
    }

    /**
     * @return Returns the iconInsets.
     */
	private Insets getIconInsets() {
        return iconInsets;
    }

    /**
     * @param iconInsets
     *            The iconInsets to set.
     */
	private void setIconInsets(Insets iconInsets) {
        this.iconInsets = iconInsets;
    }

    /**
     * @return Returns the buttonMargin.
     */
	protected int getButtonMargin() {
        return buttonMargin;
    }

    /**
     * @param buttonMargin
     *            The buttonMargin to set.
     */
	private void setButtonMargin(int buttonMargin) {
        this.buttonMargin = buttonMargin;
    }

    /**
     * @return Returns the painterResource.
     */
    public Painter getPainter() {
        return painter;
    }

    /**
     * @param painter
     *            The painter to set.
     */
	private void setPainter(Painter painter) {
        this.painter = painter;
    }

    /**
     * @return Returns the insets.
     */
    public Insets getInsets() {
        return insets;
    }

    /**
     * @param insets
     *            The insets to set.
     */
    public void setInsets(Insets insets) {
        this.insets = insets;
    }

    /**
     * @return Returns the antialiasing.
     */
    public boolean isAntialiasing() {
        return antialiasing == RenderingHints.VALUE_ANTIALIAS_ON;
    }

    /**
     * @param antialias
     *            The antialias to set.
     */
	private void setAntialiasing(boolean antialias) {
        antialiasing = antialias ? RenderingHints.VALUE_ANTIALIAS_ON :
                       RenderingHints.VALUE_ANTIALIAS_OFF;
    }

    @Override
    public void initializeCreationParameters() {
        setActiveBackground(creationParameters.getColor(BACKGROUND_COLOR_ACTIVE));
        setActiveFont(creationParameters.getColor(FONT_COLOR_ACTIVE));
        setInactiveBackground(creationParameters.getColor(BACKGROUND_COLOR));
        setInactiveFont(creationParameters.getColor(FONT_COLOR));
        setDefaultHeight(creationParameters.getInt(DEFAULT_HEIGHT));
        setFont(creationParameters.getFont(FONT));
        setInactiveBorder(creationParameters.getBorder(BORDER));
        setActiveBorder(creationParameters.getBorder(BORDER_ACTIVE));

        setDefaultIcons(creationParameters.getString(IconResourceFactory.ICON_MAP_KEY));
        setPainter((Painter) creationParameters.getProperty(PAINTER));
        setIconInsets((Insets) creationParameters.getProperty(ICON_INSETS));
        setButtonMargin(creationParameters.getInt(BUTTON_MARGIN));
        setPainter((Painter) creationParameters.getProperty(PAINTER));
        setInsets((Insets) creationParameters.getProperty(INSETS));
        setAntialiasing(creationParameters.getBoolean( ANTIALIASING));
    }

    public String getPreferredButtonUI() {
        return creationParameters.getString(UIFactory.BUTTON_KEY);
    }
}