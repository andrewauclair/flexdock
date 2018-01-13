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
package org.flexdock.docking.props;

import org.flexdock.docking.RegionChecker;

/**
 * @author Christopher Butler
 */
public interface DockingPortPropertySet {
	String REGION_CHECKER = "DockingPort.REGION_CHECKER";
	String SINGLE_TABS = "DockingPort.SINGLE_TABS";
	String TAB_PLACEMENT = "DockingPort.TAB_PLACEMENT";

	String REGION_SIZE_NORTH = "DockingPort.REGION_SIZE_NORTH";
	String REGION_SIZE_SOUTH = "DockingPort.REGION_SIZE_SOUTH";
	String REGION_SIZE_EAST = "DockingPort.REGION_SIZE_EAST";
	String REGION_SIZE_WEST = "DockingPort.REGION_SIZE_WEST";

	RegionChecker getRegionChecker();

	Boolean isSingleTabsAllowed();

	Integer getTabPlacement();

	Float getRegionInset(String region);

	void setRegionChecker(RegionChecker checker);

	void setSingleTabsAllowed(boolean allowed);

	void setTabPlacement(int placement);

	void setRegionInset(String region, float inset);

}
