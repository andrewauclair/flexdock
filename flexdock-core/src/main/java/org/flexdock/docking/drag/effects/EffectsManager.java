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
package org.flexdock.docking.drag.effects;

import org.flexdock.docking.Dockable;
import org.flexdock.docking.DockingPort;
import org.flexdock.util.ResourceManager;
import org.flexdock.util.Utilities;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Christopher Butler
 */
public class EffectsManager {
	private static final String CONFIG_URI = "org/flexdock/docking/drag/effects/drag-effects.xml";
	private static final Object LOCK = new Object();

	private static DragPreview defaultPreview;
	private static DragPreview customPreview;
	private static RubberBand customRubberband;

	static {
		prime();
	}

	public static void prime() {
		Document config = ResourceManager.getDocument(CONFIG_URI);
		defaultPreview = loadDefaultPreview(config);
	}

	public static DragPreview getPreview(Dockable dockable, DockingPort target) {
		synchronized (LOCK) {
			return customPreview == null ? defaultPreview : customPreview;
		}
	}

	public DragPreview setPreview(String implClass) {
		DragPreview preview = createPreview(implClass);
		if (implClass != null && preview == null) {
			return null;
		}

		setPreview(preview);
		return preview;
	}

	public static void setPreview(DragPreview preview) {
		synchronized (LOCK) {
			customPreview = preview;
		}
	}

	private static Document loadConfig() {
		return ResourceManager.getDocument(CONFIG_URI);
	}

	private static DragPreview createPreview(String implClass) {
		return (DragPreview) Utilities.createInstance(implClass, DragPreview.class);
	}

	// TODO The HashMap in here uses multiple types, why?
	private static HashMap loadRubberBandInfoByOS(Document config) {
		HashMap map = new HashMap();

		Element root = (Element) config.getElementsByTagName("rubber-bands").item(0);
		map.put("default", root.getAttribute("default"));
		NodeList nodes = root.getElementsByTagName("os");

		for (int i = 0; i < nodes.getLength(); i++) {
			Element osElem = (Element) nodes.item(i);
			String osName = osElem.getAttribute("name");
			NodeList items = osElem.getElementsByTagName("rubber-band");
			ArrayList classes = new ArrayList(items.getLength());
			map.put(osName, classes);
			for (int j = 0; j < items.getLength(); j++) {
				Element classElem = (Element) items.item(j);
				String className = classElem.getAttribute("class");
				classes.add(className);
			}
		}
		return map;
	}

	private static DragPreview loadDefaultPreview(Document config) {
		Element root = (Element) config.getElementsByTagName("drag-previews").item(0);
		String previewClass = root.getAttribute("default");
		DragPreview preview = createPreview(previewClass);
		if (preview != null) {
			return preview;
		}
		// unable to load the preview class.  return a no-op preview delegate instead.
		return new DefaultPreview() {
			@Override
			public void drawPreview(Graphics2D g, Polygon poly, Dockable dockable, Map dragInfo) {
				// noop
			}
		};
	}

}
