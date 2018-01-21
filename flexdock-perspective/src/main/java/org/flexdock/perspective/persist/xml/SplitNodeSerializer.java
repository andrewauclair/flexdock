/*
 * Copyright (c) 2005 FlexDock Development Team. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in the
 * Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE.
 */
package org.flexdock.perspective.persist.xml;

import org.flexdock.docking.DockingConstants;
import org.flexdock.docking.state.LayoutNode;
import org.flexdock.docking.state.tree.SplitNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.swing.*;

/**
 * Created on 2005-06-23
 *
 * @author <a href="mailto:mati@sz.home.pl">Mateusz Szczap</a>
 * @version $Id: SplitNodeSerializer.java,v 1.9 2005-07-06 18:10:48 winnetou25 Exp $
 */
public class SplitNodeSerializer extends AbstractLayoutNodeSerializer implements ISerializer {
	
	@Override
	public Element serialize(Document document, Object object) {
		SplitNode splitNode = (SplitNode) object;
		
		Element splitNodeElement = super.serialize(document, object);
		
		if (splitNode.getSiblingId() != null && !"".equals(splitNode.getSiblingId())) {
			splitNodeElement.setAttribute(PersistenceConstants.SPLIT_NODE_ATTRIBUTE_SIBLING_ID, splitNode.getSiblingId());
		}
		splitNodeElement.setAttribute(PersistenceConstants.SPLIT_NODE_ATTRIBUTE_ORIENTATION, splitNode.getOrientationDesc());
		splitNodeElement.setAttribute(PersistenceConstants.SPLIT_NODE_ATTRIBUTE_REGION, splitNode.getRegionDesc());
		splitNodeElement.setAttribute(PersistenceConstants.SPLIT_NODE_ATTRIBUTE_PERCENTAGE, String.valueOf(splitNode.getPercentage()));
		
		if (splitNode.getDockingRegion() != null) {
			splitNodeElement.setAttribute(PersistenceConstants.SPLIT_NODE_ATTRIBUTE_DOCKING_REGION, splitNode.getDockingRegion().toString().toLowerCase());
		}
		
		return splitNodeElement;
	}
	
	@Override
	protected Element getElement(Document document, Object o) {
		return document.createElement(PersistenceConstants.SPLIT_NODE_ELEMENT_NAME);
	}
	
	@Override
	public Object deserialize(Element element) {
		
		SplitNode splitNode = (SplitNode) super.deserialize(element);
		
		String siblingId = element.getAttribute(PersistenceConstants.SPLIT_NODE_ATTRIBUTE_SIBLING_ID);
		String orientationString = element.getAttribute(PersistenceConstants.SPLIT_NODE_ATTRIBUTE_ORIENTATION);
		String regionString = element.getAttribute(PersistenceConstants.SPLIT_NODE_ATTRIBUTE_REGION);
		String percentage = element.getAttribute(PersistenceConstants.SPLIT_NODE_ATTRIBUTE_PERCENTAGE);
		String dockingRegion = element.getAttribute(PersistenceConstants.SPLIT_NODE_ATTRIBUTE_DOCKING_REGION);
		
		int orientation = DockingConstants.UNINITIALIZED;
		if (orientationString.equals("vertical")) {
			orientation = JSplitPane.VERTICAL_SPLIT;
		}
		else if (orientationString.equals("horizontal")) {
			orientation = JSplitPane.HORIZONTAL_SPLIT;
		}
		
		int region = DockingConstants.UNINITIALIZED;
		switch (regionString) {
		case "top":
			region = SwingConstants.TOP;
			break;
		case "bottom":
			region = SwingConstants.BOTTOM;
			break;
		case "left":
			region = SwingConstants.LEFT;
			break;
		case "right":
			region = SwingConstants.RIGHT;
			break;
		}
		
		splitNode.setOrientation(orientation);
		splitNode.setRegion(region);
		splitNode.setPercentage(Float.parseFloat(percentage));
		if (siblingId != null && !"".equals(siblingId)) {
			splitNode.setSiblingId(siblingId);
		}
		if (dockingRegion != null && dockingRegion.length() != 0) {
			splitNode.setDockingRegion(DockingConstants.Region.valueOf(dockingRegion));
		}
		
		return splitNode;
	}
	
	@Override
	protected LayoutNode createLayoutNode() {
		return new SplitNode(-1, -1, -1.0f, null);
	}
}
