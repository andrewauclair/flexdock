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

import org.flexdock.plaf.resources.ResourceHandler;
import org.flexdock.plaf.resources.ResourceHandlerFactory;
import org.flexdock.util.ResourceManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.HashMap;

/**
 * @author Christopher Butler
 */
public final class Configurator {
	private static final String DEFAULT_PREFS_URI = "org/flexdock/plaf/flexdock-themes-default.xml";
	private static final String PREFS_URI = "flexdock-themes.xml";

	private Configurator() {
		//does nothing
	}

	private static Document loadUserPrefs() {
		return ResourceManager.getDocument(PREFS_URI);
	}

	private static Document loadDefaultPrefs() {
		return ResourceManager.getDocument(DEFAULT_PREFS_URI);
	}

	public static HashMap<String, Element> getNamedElementsByTagName(String tagName) {
		if (isNull(tagName)) {
			return null;
		}

		HashMap<String, Element> cache = new HashMap<>(256);
		// load defaults
		Document defaults = Configurator.loadDefaultPrefs();
		loadNamedElementsByTagName(defaults, tagName, cache);
		// overwrite/add with user prefs
		Document user = Configurator.loadUserPrefs();
		loadNamedElementsByTagName(user, tagName, cache);

		return cache;
	}

	private static void loadNamedElementsByTagName(Document document, String tagName, HashMap<String, Element> cache) {
		if (document == null) {
			return;
		}

		NodeList elements = document.getElementsByTagName(tagName);

		for (int i = 0; i < elements.getLength(); i++) {
			Element elem = (Element) elements.item(i);
			String key = elem.getAttribute(XMLConstants.NAME_KEY);
			boolean inherit = "true".equals(elem.getAttribute(XMLConstants.INHERITS_KEY));
			if (!isNull(key)) {

				if (inherit) {
					// mark as overridden, so we don't overwrite it in the cache
					Element oldValue = cache.get(key);
					if (oldValue != null) {
						cache.put(XMLConstants.OVERRIDDEN_KEY + key, oldValue);
					}
				}
				cache.put(key, elem);
			}
		}
	}

	public static PropertySet[] getProperties(String tagName) {
		HashMap<String, Element> map = getNamedElementsByTagName(tagName);
		if (map == null) {
			return new PropertySet[0];
		}

		String[] names = map.keySet().toArray(new String[0]);
		return getProperties(names, map);
	}

	public static PropertySet getProperties(String name, String tagName) {
		HashMap<String, Element> map = getNamedElementsByTagName(tagName);
		if (map == null) {
			return null;
		}
		return getProperties(name, map);
	}

	public static PropertySet[] getProperties(String[] names, String tagName) {
		HashMap<String, Element> map = names == null ? null : getNamedElementsByTagName(tagName);
		if (map == null) {
			return new PropertySet[0];
		}
		return getProperties(names, map);
	}

	private static PropertySet[] getProperties(String[] names, HashMap<String, Element> cache) {
		PropertySet[] properties = new PropertySet[names.length];
		for (int i = 0; i < names.length; i++) {
			properties[i] = getProperties(names[i], cache);
		}
		return properties;
	}

	private static PropertySet getProperties(String elemName, HashMap<String, Element> cache) {
		Element elem = isNull(elemName) ? null : cache.get(elemName);
		if (elem == null) {
			return null;
		}

		PropertySet set = new PropertySet();
		set.setName(elemName);

		// load all the parent properties first, so we can add/overwrite our own later
		String parentName = elem.getAttribute(XMLConstants.EXTENDS_KEY);
		PropertySet parent = isNull(parentName) ? null : getProperties(parentName, cache);
		if (parent != null) {
			set.setAll(parent);
		}

		// check to see if we're supposed to inherit from an overridden element
		if ("true".equalsIgnoreCase(elem.getAttribute(XMLConstants.INHERITS_KEY))) {
			PropertySet overridden = getProperties(XMLConstants.OVERRIDDEN_KEY + elemName, cache);
			if (overridden != null) {
				set.setAll(overridden);
			}
		}

		// get the default handler name
		String propertyHandlerName = getPropertyHandlerName(elem);

		NodeList list = elem.getElementsByTagName(XMLConstants.PROPERTY_KEY);
		int len = list.getLength();
		for (int i = 0; i < len; i++) {
			elem = (Element) list.item(i);
			String key = elem.getAttribute(XMLConstants.NAME_KEY);
			if (!isNull(key)) {
				String value = elem.getAttribute(XMLConstants.VALUE_KEY);
				String handler = elem.getAttribute(XMLConstants.HANDLER_KEY);
				Object resource = getResource(value, handler, propertyHandlerName);
				if (resource != null) {
					set.setProperty(key, resource);
				}
			}
		}
		return set;
	}

	private static String getPropertyHandlerName(Element elem) {
		String handlerName = elem.getAttribute(XMLConstants.PROP_HANDLER_KEY);
		if (isNull(handlerName)) {
			handlerName = ResourceHandlerFactory.getPropertyHandler(elem.getTagName());
		}
		return isNull(handlerName) ? null : handlerName;
	}

	public static Object getResource(String stringValue, String currentHandlerName, String defaultHandlerName) {
		String handlerName = isNull(currentHandlerName) ? defaultHandlerName : currentHandlerName;
		if (isNull(handlerName)) {
			return nullify(stringValue);
		}

		ResourceHandler handler = ResourceHandlerFactory.getResourceHandler(handlerName);
		return handler == null ? nullify(stringValue) : handler.getResource(stringValue);
	}

	private static String nullify(String data) {
		return isNull(data) ? null : data;
	}


	public static boolean isNull(String data) {
		return data == null || data.trim().length() == 0;
	}
}
