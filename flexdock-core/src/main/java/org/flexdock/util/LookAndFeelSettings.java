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
package org.flexdock.util;

import com.l2fprod.gui.plaf.skin.Skin;
import com.l2fprod.gui.plaf.skin.SkinLookAndFeel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.swing.*;
import java.awt.*;
import java.util.Hashtable;
import java.util.Properties;

/**
 * @author Christopher Butler
 */
public class LookAndFeelSettings {
	public static final String TAB_PANE_BORDER_INSETS = "TabbedPane.contentBorderInsets";
	private static final String DOM_RESOURCE = "org/flexdock/util/laf-defaults.xml";
	private static final String TAB_EDGE_INSET_KEY = "tabEdgeInset";

	private static final String PROPERTY_KEY = "property";
	private static final String KEY = "key";
	private static final String VALUE = "value";


	private static final LookAndFeelSettings SINGLETON = new LookAndFeelSettings();
	private Hashtable<String, Properties> propertyMappings;
	private boolean skinLFSupport;

	private LookAndFeelSettings() {
		propertyMappings = new Hashtable<>();
		Document document = ResourceManager.getDocument(DOM_RESOURCE);
		NodeList nodes = document.getDocumentElement().getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node instanceof Element) {
				String key = ((Element) node).getTagName();
				Properties value = getProperties((Element) node);
				propertyMappings.put(key, value);
			}
		}
		skinLFSupport = isSkinLFInClasspath();
	}

	private Properties getProperties(Element element) {
		Properties p = new Properties();

		NodeList nodes = element.getElementsByTagName(PROPERTY_KEY);
		for (int i = 0; i < nodes.getLength(); i++) {
			Element prop = (Element) nodes.item(i);
			String key = prop.getAttribute(KEY);
			String value = prop.getAttribute(VALUE);
			if (key != null && value != null) {
				p.setProperty(key, value);
			}
		}
		return p;
	}

	private String getProperty(String propType, String key) {
		Properties p = key == null ? null : propertyMappings.get(propType);
		return p == null ? null : p.getProperty(key);
	}

	private void setProperty(String propType, String key, String value) {
		Properties p = key == null ? null : propertyMappings.get(propType);
		if (p != null) {
			if (value == null) {
				p.remove(key);
			}
			else {
				p.setProperty(key, value);
			}
		}
	}

	public static boolean isSkinLFSupported() {
		return SINGLETON.skinLFSupport;
	}

	public static int getTabEdgeInset(int tabPlacement) {
		String plafKey = SINGLETON.getCurrentPlafName();
		String edgeStr = SINGLETON.getProperty(TAB_EDGE_INSET_KEY, plafKey);
		Integer edge = edgeStr == null ? null : Integer.parseInt(edgeStr);

		if (edge != null && edge > 0) {
			return edge;
		}

		Insets tabInsets = UIManager.getInsets(TAB_PANE_BORDER_INSETS);
		if (tabInsets == null) {
			return 1;
		}

		switch (tabPlacement) {
		case JTabbedPane.TOP:
			return tabInsets.top;
		case JTabbedPane.BOTTOM:
			return tabInsets.bottom;
		case JTabbedPane.LEFT:
			return tabInsets.left;
		case JTabbedPane.RIGHT:
			return tabInsets.right;
		default:
			return 1;
		}
	}

	public static void setTabEdgeInset(String plafKey, int inset) {
		if (plafKey != null) {
			SINGLETON.setProperty(TAB_EDGE_INSET_KEY, plafKey, String.valueOf(inset));
		}
	}

	private String getCurrentPlafName() {
		LookAndFeel laf = UIManager.getLookAndFeel();
		if (laf == null) {
			return null;
		}

		// return a special case for SkinLF
		if (isSkinLFSupported()) {
			if (laf.getClass() == SkinLookAndFeel.class) {
				Skin skin = SkinLookAndFeel.getSkin();
				return skin == null ? null : skin.getClass().getName();
			}
		}

		// return the className for the current plaf
		return laf.getClass().getName();
	}

	private boolean isSkinLFInClasspath() {
		try {
			// try to load the class
			Class c = SkinLookAndFeel.class;
			// if the class loaded, then we can return true
			return true;
		}
		catch (Throwable t) {
			// if the class didn't load, then return false;
			return false;
		}
	}
}
