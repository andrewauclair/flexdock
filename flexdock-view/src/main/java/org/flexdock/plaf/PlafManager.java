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
package org.flexdock.plaf;

import org.flexdock.plaf.mappings.PlafMappingFactory;
import org.flexdock.plaf.theme.Theme;
import org.flexdock.plaf.theme.UIFactory;
import org.flexdock.util.RootWindow;
import org.flexdock.view.Button;
import org.flexdock.view.View;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Hashtable;
import java.util.Properties;

/**
 * @author Christopher Butler
 */
public class PlafManager {
    private static final String PREFERRED_THEME_KEY = "preferred.theme";

    private static final String UI_CHANGE_EVENT = "lookAndFeel";

    private static final Hashtable UI_DEFAULTS = new Hashtable();

    private static final Hashtable CUSTOM_THEMES = new Hashtable();

    static {
        initialize();
    }

    private static void initialize() {
        installPreferredTheme(false);
        // install an updater so we can keep up with changes in the installed
        // plaf
        UIManager.addPropertyChangeListener(new UiUpdater());
    }

    public static String getSystemThemeName() {
        return PlafMappingFactory.getInstalledPlafReference();
    }

	public static void setPreferredTheme(String themeName, boolean reload) {
        String oldPref = (String) UI_DEFAULTS.get(PREFERRED_THEME_KEY);

        if (Configurator.isNull(themeName)) {
            UI_DEFAULTS.remove(PREFERRED_THEME_KEY);
        } else {
            UI_DEFAULTS.put(PREFERRED_THEME_KEY, themeName);
        }

        String newPref = (String) UI_DEFAULTS.get(PREFERRED_THEME_KEY);

        // this will handle the case where we switch from null to something
        // else,
        // vice versa, or a new string value
        boolean themeChanged = (oldPref == null ? newPref != null : !oldPref.equals(newPref))
            && (oldPref == null || newPref == null || !oldPref.equals(newPref));
        if (reload || themeChanged) {
            installPreferredTheme();
        }
    }

	private static void installPreferredTheme(boolean update) {
        Theme theme = getPreferredTheme();

        UI_DEFAULTS.clear();
        setProperty(View.class, theme.getViewUI());
        setProperty(Button.class, theme.getButtonUI());

        if (update) {
            RootWindow[] windows = RootWindow.getVisibleWindows();
			for (RootWindow window : windows) {
				window.updateComponentTreeUI();
			}
        }
    }

	static void installPreferredTheme() {
        installPreferredTheme(true);
    }

    private static Theme getPreferredTheme() {
        Theme theme = null;
        String themeName = (String) UI_DEFAULTS.get(PREFERRED_THEME_KEY);
        if (themeName != null) {
            theme = (Theme) CUSTOM_THEMES.get(themeName);
            if (theme == null) {
                theme = UIFactory.getTheme(themeName);
            }
        }
        if (theme == null) {
            theme = UIFactory.getTheme(getSystemThemeName());
        }
        if (theme == null) {
            theme = UIFactory.getTheme(UIFactory.DEFAULT);
        }
        return theme;
    }

	public static Theme setCustomTheme(String themeName, Properties p) {
        return loadCustomTheme(themeName, p, true);
    }

	private static Theme loadCustomTheme(String themeName, Properties p,
										 boolean exclusive) {
        if (Configurator.isNull(themeName) || p == null) {
            return null;
        }

        Theme theme = UIFactory.createTheme(p);
        if (theme != null) {
            theme.setName(themeName);
            if (exclusive) {
                CUSTOM_THEMES.clear();
            }
            CUSTOM_THEMES.put(themeName, theme);
        }
        return theme;
    }

	private static void setProperty(Object key, Object value) {
        if (key != null && value != null) {
            UI_DEFAULTS.put(key, value);
        }
    }

    /**
     * Returns the appropriate {@code ComponentUI} implementation for
     * {@code target}. In case the component is a member of the installed look
     * and feel, this method first queries {@code UIManager.getUI(target)}
     * before attempting to resolve it locally.
     *
     * @param target
     *            the {@code JComponent} to return the {@code ComponentUI} for
     * @return the {@code ComponentUI} object for {@code target}
     * @throws NullPointerException
     *             if {@code target} is {@code null}
     * @see UIManager#getUI
     */
    public static ComponentUI getUI(JComponent target) {
		return (ComponentUI) UI_DEFAULTS.get(target.getClass());
    }

    private static class UiUpdater implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (UI_CHANGE_EVENT.equals(evt.getPropertyName())
                && evt.getOldValue() != evt.getNewValue()) {
                installPreferredTheme();
            }
        }
    }
}
