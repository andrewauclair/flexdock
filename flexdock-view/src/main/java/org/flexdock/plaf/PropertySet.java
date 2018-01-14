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

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.*;
import java.util.List;




/**
 * @author Christopher Butler
 */
public class PropertySet {

	private HashMap<String, Object> properties;
    private String name;

    public PropertySet() {
		properties = new HashMap<>();
    }

	public void setAll(PropertySet set) {
        if(set!=null) {
            properties.putAll(set.properties);
        }
    }

    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }

    public Object getProperty(String key) {
        return properties.get(key);
    }

    public Color getColor(String key) {
        Object property = getProperty(key);
        return property instanceof Color? (Color)property: null;
    }

    public Font getFont(String key) {
        Object property = getProperty(key);
        return property instanceof Font? (Font)property: null;
    }

	public Icon getIcon(String key) {
        Object property = getProperty(key);
        return property instanceof Icon? (Icon)property: null;
    }

    public Action getAction(String key) {
        Object property = getProperty(key);
        return property instanceof Action? (Action)property: null;
    }

    public String getString(String key) {
        Object property = getProperty(key);
        return property instanceof String? (String)property: null;
    }

    public Border getBorder(String key) {
        Object property = getProperty(key);
        return property instanceof Border? (Border)property: null;
    }

    public String[] getStrings(String[] keys) {
        if(keys==null) {
            return null;
        }

        String[] values = new String[keys.length];
        for(int i=0; i<values.length; i++) {
            values[i] = getString(keys[i]);
        }
        return values;
    }

    public int getInt(String key) {
        String string = getString(key);
        if(string==null) {
            return 0;
        }

        try {
            return Integer.parseInt(string);
        } catch(NumberFormatException e) {
            System.err.println("Exception: " +e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

	public boolean getBoolean(String key) {
        String string = getString(key);
        if(string==null) {
            return false;
        }

        try {
			return Boolean.valueOf(string);
        } catch(NumberFormatException e) {
            System.err.println("Exception: " +e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

	public Iterator<String> keys() {
        return properties.keySet().iterator();
    }
    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }
    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

	private int size() {
        return properties.size();
    }

	public List<String> getNumericKeys(boolean sort) {
		ArrayList<String> list = new ArrayList<>(size());
		for (Object o : properties.keySet()) {
			String key = (String) o;
			if (isNumeric(key)) {
				list.add(key);
			}
		}

        if(sort) {
			list.sort(Comparator.comparingInt(Integer::parseInt));
        }

        return list;
    }

	// TODO Is there a better way to check if a string is an integer?
	private boolean isNumeric(String string) {
		try {
			Integer.parseInt(string);
			return true;
		} catch(NumberFormatException e) {
			return false;
		}
	}

	public Class<?> toClass(String key) throws ClassNotFoundException {
        String type = getString(key);
        return resolveClass(type);
    }

	protected Class<?> resolveClass(String className) throws ClassNotFoundException {
        if(className==null) {
            return null;
        }

        if("int".equals(className)) {
            return int.class;
        }
        if("long".equals(className)) {
            return long.class;
        }
        if("boolean".equals(className)) {
            return boolean.class;
        }
        if("float".equals(className)) {
            return float.class;
        }
        if("double".equals(className)) {
            return double.class;
        }
        if("byte".equals(className)) {
            return byte.class;
        }
        if("short".equals(className)) {
            return short.class;
        }

        return Class.forName(className);
    }

    @Override
    public String toString() {
        return "PropertySet[name=\"" + name + "\"; hashmap=" + properties + "]";
    }
}
