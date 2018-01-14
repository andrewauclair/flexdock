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

import org.flexdock.plaf.Configurator;
import org.flexdock.plaf.IFlexViewComponentUI;
import org.flexdock.plaf.PlafManager;
import org.flexdock.plaf.PropertySet;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Properties;

import static org.flexdock.plaf.XMLConstants.*;

/**
 * @author Christopher Butler
 */
public class UIFactory {

	public static final String DEFAULT = "default";
	private static final String THEME_KEY = "theme";
	public static final String VIEW_KEY = "view-ui";
	public static final String TITLEBAR_KEY = "titlebar-ui";
	public static final String BUTTON_KEY = "button-ui";
	private static final HashMap<String, IFlexViewComponentUI> VIEW_UI_CACHE = new HashMap<>();
	private static final HashMap<String, IFlexViewComponentUI> TITLEBAR_UI_CACHE = new HashMap<>();
	private static final HashMap<String, IFlexViewComponentUI> BUTTON_UI_CACHE = new HashMap<>();
	private static final HashMap<String, Theme> THEME_UI_CACHE = new HashMap<>();

	private static ViewUI getViewUI(String name) {
		return (ViewUI) getUI(name, VIEW_UI_CACHE, VIEW_KEY, ViewUI.class);
	}

	private static ViewUI getViewUI(Properties p) {
		return (ViewUI) getUI(p, VIEW_UI_CACHE, VIEW_KEY, ViewUI.class);
	}

	private static TitlebarUI getTitlebarUI(String name) {
		return (TitlebarUI) getUI(name, TITLEBAR_UI_CACHE, TITLEBAR_KEY, TitlebarUI.class);
	}

	private static TitlebarUI getTitlebarUI(Properties p) {
		return (TitlebarUI) getUI(p, TITLEBAR_UI_CACHE, TITLEBAR_KEY, TitlebarUI.class);
	}

	private static ButtonUI getButtonUI(String name) {
		return (ButtonUI) getUI(name, BUTTON_UI_CACHE, BUTTON_KEY, ButtonUI.class);
	}

	private static ButtonUI getButtonUI(Properties p) {
		return (ButtonUI) getUI(p, BUTTON_UI_CACHE, BUTTON_KEY, ButtonUI.class);
	}


	public static Theme getTheme(String name) {
		if (Configurator.isNull(name)) {
			return null;
		}

		Theme theme = THEME_UI_CACHE.get(name);
		if (theme == null) {
			theme = loadTheme(name);
			if (theme != null) {
				synchronized (THEME_UI_CACHE) {
					THEME_UI_CACHE.put(name, theme);
				}
			}
		}
		return theme;
	}

	private static IFlexViewComponentUI getUI(Properties p, HashMap<String, IFlexViewComponentUI> cache, String tagName, Class<?> rootClass) {
		if (p == null || !p.containsKey(tagName)) {
			return null;
		}

		String name = p.getProperty(tagName);
		return getUI(name, cache, tagName, rootClass);
	}

	private static synchronized IFlexViewComponentUI getUI(String name, HashMap<String, IFlexViewComponentUI> cache, String tagName, Class<?> rootClass) {
		if (Configurator.isNull(name)) {
			return null;
		}

		IFlexViewComponentUI ui = cache.get(name);
		if (ui == null) {
			ui = loadUI(name, tagName, rootClass);
			if (ui != null) {
				cache.put(name, ui);
			}
		}
		return ui;
	}

	private static IFlexViewComponentUI loadUI(String name, String tagName, Class<?> rootClass) {
		PropertySet properties = Configurator.getProperties(name, tagName);
		if (properties == null) {
			return null;
		}

		String classname = properties.getString(CLASSNAME_KEY);
		Class<?> implClass = loadUIClass(classname, rootClass);

		try {
			IFlexViewComponentUI ui = (IFlexViewComponentUI) implClass.newInstance();
			ui.setCreationParameters(properties);
			return ui;
		}
		catch (Exception e) {
			// we use public, no-argument constructors, so if this happens, we
			// have a configuration error.
			System.err.println("Exception: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	private static Class<?> loadUIClass(String classname, Class<?> rootClass) {
		if (Configurator.isNull(classname)) {
			return rootClass;
		}

		Class<?> implClass;
		try {
			implClass = Class.forName(classname);
			if (!rootClass.isAssignableFrom(implClass)) {
				implClass = null;
			}
		}
		catch (ClassNotFoundException e) {
			System.err.println("Exception: " + e.getMessage());
			implClass = null;
		}
		return implClass == null ? rootClass : implClass;
	}

	private static Theme loadTheme(String themeName) {
		HashMap<String, Element> map = Configurator.getNamedElementsByTagName(THEME_KEY);
		if (map == null) {
			return null;
		}
		return loadTheme(themeName, map);
	}

	private static Theme loadTheme(String themeName, HashMap<String, Element> cache) {
		Element themeElem = cache.get(themeName);
		if (themeElem == null) {
			return null;
		}

		// if we're an indirect reference to a different theme, then return that theme
		String redirect = themeElem.getAttribute(REFERENCE_KEY);
		if (!Configurator.isNull(redirect)) {
			return loadTheme(redirect, cache);
		}

		// if we're a child of another theme, then load the parent and
		// add our properties afterward
		String parentName = themeElem.getAttribute(EXTENDS_KEY);
		Theme theme = Configurator.isNull(parentName) ? new Theme() : loadTheme(parentName, cache);
		if (theme == null) {
			theme = new Theme();
		}

		String name = themeElem.getAttribute(NAME_KEY);
		String desc = themeElem.getAttribute(DESC_KEY);
		String view = themeElem.getAttribute(VIEW_KEY);

		theme.setName(name);
		theme.setDescription(desc);

		ViewUI viewUI = Configurator.isNull(view) ? getViewUI(DEFAULT) : getViewUI(view);
		TitlebarUI titlebarUI = viewUI == null ? getTitlebarUI(DEFAULT) : getTitlebarUI(viewUI.getPreferredTitlebarUI());
		ButtonUI buttonUI = titlebarUI == null ? getButtonUI(DEFAULT) : getButtonUI(titlebarUI.getPreferredButtonUI());

		theme.setViewUI(viewUI);
		theme.setTitlebarUI(titlebarUI);
		theme.setButtonUI(buttonUI);

		return theme;
	}

	public static Theme createTheme(Properties p) {
		if (p == null) {
			return null;
		}

		Theme base = getTheme(PlafManager.getSystemThemeName());

		ViewUI view = getViewUI(p);
		if (view == null && base != null) {
			view = base.getViewUI();
		}
		if (view == null) {
			view = getViewUI(DEFAULT);
		}

		TitlebarUI titlebar = getTitlebarUI(p);
		if (titlebar == null) {
			titlebar = getTitlebarUI(view.getPreferredTitlebarUI());
		}
		if (titlebar == null && base != null) {
			titlebar = base.getTitlebarUI();
		}
		if (titlebar == null) {
			titlebar = getTitlebarUI(DEFAULT);
		}

		ButtonUI button = getButtonUI(p);
		if (button == null) {
			button = getButtonUI(titlebar.getPreferredButtonUI());
		}
		if (button == null && base != null) {
			button = base.getButtonUI();
		}
		if (button == null) {
			button = getButtonUI(DEFAULT);
		}

		Theme theme = new Theme();
		theme.setName(p.getProperty(NAME_KEY, "custom"));
		theme.setDescription(p.getProperty(DESC_KEY, "Custom Theme"));
		theme.setViewUI(view);
		theme.setTitlebarUI(titlebar);
		theme.setButtonUI(button);
		return theme;
	}
}
