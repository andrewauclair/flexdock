/*
 * Created on Feb 28, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.flexdock.windowing.plaf;

import java.util.HashMap;

import org.flexdock.util.ResourceManager;
import org.flexdock.windowing.plaf.resources.ResourceHandler;
import org.flexdock.windowing.plaf.resources.ResourceHandlerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author Christopher Butler
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Configurator implements XMLConstants {
	public static final String DEFAULT_PREFS_URI = "org/flexdock/windowing/view-prefs-default.xml";
	public static final String PREFS_URI = "view-prefs.xml";


	
	public static Document loadUserPrefs() {
		return ResourceManager.getDocument(PREFS_URI);
	}
	
	public static Document loadDefaultPrefs() {
		return ResourceManager.getDocument(DEFAULT_PREFS_URI);
	}
	
	public static HashMap getNamedElementsByTagName(String tagName) {
		if(isNull(tagName))
			return null;
		
		HashMap cache = new HashMap(256);
		// load defaults
		Document defaults = Configurator.loadDefaultPrefs(); 
		loadNamedElementsByTagName(defaults, tagName, cache);
		// overwrite/add with user prefs
		Document user = Configurator.loadUserPrefs();
		loadNamedElementsByTagName(user, tagName, cache);
		return cache;
	}
	
	private static void loadNamedElementsByTagName(Document document, String tagName, HashMap cache) {
		if(document==null)
			return;
		
		NodeList handlers = document.getElementsByTagName(HANDLER_KEY);
		HashMap map = new HashMap(handlers.getLength());
		
		for(int i=0; i<handlers.getLength(); i++) {
			Element elem = (Element)handlers.item(i);
			String key = elem.getAttribute(NAME_KEY);
			if(!isNull(key))
				map.put(key, elem);
		}
	}
	
	public static PropertySet getProperties(String name, String tagName) {
		HashMap map = getNamedElementsByTagName(tagName);
		if(map==null)
			return null;
	
		return getProperties(name, map);
	}
	
	private static PropertySet getProperties(String elemName, HashMap cache) {
		Element elem = (Element)cache.get(elemName);
		if(elem==null)
			return null;
		
		PropertySet set = new PropertySet();

		// load all the parent properties first, so we can add/overwrite our own later
		String parentName = elem.getAttribute(EXTENDS_KEY);
		PropertySet parent = isNull(parentName)? null: getProperties(parentName, cache);
		if(parent!=null)
			set.setAll(parent);
		
		NodeList list = elem.getElementsByTagName(PROPERTY_KEY);
		int len = list.getLength();
		for(int i=0; i<len; i++) {
			elem = (Element)list.item(i);
			String key = elem.getAttribute(NAME_KEY);
			if(!isNull(key)) {
				String value = elem.getAttribute(VALUE_KEY);
				String handler = elem.getAttribute(HANDLER_KEY);
				Object resource = getResource(value, handler);
				if(resource!=null) {
					set.setProperty(key, resource); 
				}
			}
		}
		return set;
	}
	
	public static Object getResource(String stringValue, String handlerName) {
		if(isNull(handlerName))
			return nullify(stringValue);
		
		ResourceHandler handler = ResourceHandlerFactory.getResourceHandler(handlerName);
		return handler==null? nullify(stringValue): handler.getResource(stringValue);
	}
	
	private static String nullify(String data) {
		return isNull(data)? null: data;
	}


	public static boolean isNull(String data) {
		data = data==null? null: data.trim();
		return data==null || data.length()==0;
	}
}
