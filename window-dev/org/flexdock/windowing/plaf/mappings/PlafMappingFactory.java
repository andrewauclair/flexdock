/*
 * Created on Feb 28, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.flexdock.windowing.plaf.mappings;

import java.util.HashMap;
import java.util.Iterator;

import javax.swing.LookAndFeel;
import javax.swing.UIManager;

import org.flexdock.windowing.plaf.Configurator;
import org.flexdock.windowing.plaf.XMLConstants;
import org.w3c.dom.Element;

/**
 * @author Christopher Butler
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class PlafMappingFactory implements XMLConstants {
	public static final String PLAF_KEY = "plaf";
	private static final HashMap plafMappings = loadPlafMappings();
	
	
	public static String getInstalledPlafReference() {
		LookAndFeel currentPlaf = UIManager.getLookAndFeel();
		if(currentPlaf==null)
			return null;
		
		String key = currentPlaf.getClass().getName();
		return getPlafReference(key);
	}
	
	public static String getPlafReference(String key) {
		if(key==null)
			return null;
		
		Object value = plafMappings.get(key);
		if(value instanceof String)
			return (String)value;
		
		// if not a String, then we must have a RefResolver
		if(value instanceof RefResolver) {
			RefResolver resolver = (RefResolver)value;
			return resolver.getRef(key);
		}
		return null;
	}
	
	private static HashMap loadPlafMappings() {
		HashMap elements = Configurator.getNamedElementsByTagName(PLAF_KEY);
		HashMap mappings = new HashMap(elements.size());
		
		for(Iterator it=elements.keySet().iterator(); it.hasNext();) {
			Element elem = (Element)it.next();
			String key = elem.getAttribute(NAME_KEY);
			String ref = elem.getAttribute(REFERENCE_KEY);
			String resolver = elem.getAttribute(HANDLER_KEY);
			Object value = createPlafMapping(ref, resolver);
			mappings.put(key, value);
		}
		return mappings;
	}

	
	private static Object createPlafMapping(String refName, String resolverName) {
		if(Configurator.isNull(resolverName))
			return refName;
		
		RefResolver resolver = null;
		try {
			Class clazz = Class.forName(resolverName);
			// must be a type of PlafBasedViewResolver
			resolver = (RefResolver)clazz.newInstance();
		} catch(Exception e) {
			System.err.println("Error trying to create new instance of '" +resolverName + "'.");
			e.printStackTrace();
			return refName;
		}

		// setup the default value on the resolver and return
		resolver.setDefaultRef(refName);
		return resolver;
	}
}
