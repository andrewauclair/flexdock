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
package org.flexdock.docking.defaults;

import org.flexdock.docking.Dockable;
import org.flexdock.docking.DockingManager;
import org.flexdock.docking.DockingPort;
import org.flexdock.docking.RegionChecker;

import java.awt.*;

import static org.flexdock.docking.DockingConstants.Region;

/**
 * @author Christopher Butler
 */
public class DefaultRegionChecker implements RegionChecker {

	/**
	 * Returns the docking region of the supplied {@code Component} that
	 * contains the coordinates of the specified {@code Point}. If either
	 * {@code comp} or {@code point} is {@code null}, then
	 * {@code UNKNOWN_REGION} is returned. If the specified {@code Component}
	 * bounds do not contain the supplied {@code Point}, then
	 * {@code UNKNOWN_REGION} is returned.
	 * <p>
	 * This implementation assumes that {@code comp} is a {@code Component}
	 * embedded within a {@code DockingPort}. If {@code comp} is itself a
	 * {@code DockingPort}, then {@code CENTER_REGION} is returned. Otherwise,
	 * the returned region is based upon a section of the bounds of the
	 * specified {@code Component} relative to the containing
	 * {@code DockingPort}.
	 * <p>
	 * This method divides the specified {@code Component's} bounds into four
	 * {@code Rectangles} determined by {@code getNorthRegion(Component c)},
	 * {@code getSouthRegion(Component c)}, {@code getEastRegion(Component c)},
	 * and {@code getWestRegion(Component c)}, respectively. Each
	 * {@code Rectangle} is then checked to see if it contains the specified
	 * {@code Point}. The order of precedence is NORTH, SOUTH, EAST, and then
	 * WEST. If the specified {@code Point} is contained by the
	 * {@code Component} bounds but none of the sub-{@code Rectangles}, then
	 * {@code CENTER_REGION} is returned.
	 * <p>
	 * For NORTH and SOUTH {@code Rectangles}, the distance is checked between
	 * the top/bottom and left or right edge of the regional bounds. If the
	 * horizontal distance to the regional edge is smaller than the vertical
	 * distance, then EAST or WEST takes precendence of NORTH or SOUTH. This
	 * allows for proper determination between "northeast", "northwest",
	 * "southeast", and "southwest" cases.
	 *
	 * @param component  the {@code Component} whose region is to be examined.
	 * @param point the coordinates whose region is to be determined.
	 * @return the docking region containing the specified {@code Point}.
	 * @see RegionChecker#getRegion(Component, Point)
	 * @see #getNorthRegion(Component)
	 * @see #getSouthRegion(Component)
	 * @see #getEastRegion(Component)
	 * @see #getWestRegion(Component)
	 */
	@Override
	public Region getRegion(Component component, Point point) {
		if (component == null || point == null) {
			return null;
		}

		// make sure the point is actually inside of the target dockingport
		Rectangle targetArea = component.getBounds();
		// if our target component is the dockingport itself, then getBounds()
		// would
		// have returned a target area relative to the dockingport's parent.
		// reset
		// relative to the dockingport.
		if (component instanceof DockingPort) {
			targetArea.setLocation(0, 0);
		}
		if (!targetArea.contains(point)) {
			return null;
		}

		// if our target component is the dockingport, then the dockingport is
		// currently empty and all points within it are in the CENTER
		if (component instanceof DockingPort) {
			return Region.CENTER;
		}

		// start with the north region
		Rectangle north = getNorthRegion(component);
		int rightX = north.x + north.width;
		if (north.contains(point)) {
			// check NORTH_WEST
			Rectangle west = getWestRegion(component);
			if (west.contains(point)) {
				Polygon westPoly = new Polygon();
				westPoly.addPoint(0, 0);
				westPoly.addPoint(0, north.height);
				westPoly.addPoint(west.width, north.height);
				return westPoly.contains(point) ? Region.WEST : Region.NORTH;
			}
			// check NORTH_EAST
			Rectangle east = getEastRegion(component);
			if (east.contains(point)) {
				Polygon eastPoly = new Polygon();
				eastPoly.addPoint(rightX, 0);
				eastPoly.addPoint(rightX, north.height);
				eastPoly.addPoint(east.x, north.height);
				return eastPoly.contains(point) ? Region.EAST : Region.NORTH;
			}
			return Region.NORTH;
		}

		// check with the south region
		Rectangle south = getSouthRegion(component);
		int bottomY = south.y + south.height;
		if (south.contains(point)) {
			// check SOUTH_WEST
			Rectangle west = getWestRegion(component);
			if (west.contains(point)) {
				Polygon westPoly = new Polygon();
				westPoly.addPoint(0, south.y);
				westPoly.addPoint(west.width, south.y);
				westPoly.addPoint(0, bottomY);
				return westPoly.contains(point) ? Region.WEST : Region.SOUTH;
			}
			// check SOUTH_EAST
			Rectangle east = getEastRegion(component);
			if (east.contains(point)) {
				Polygon eastPoly = new Polygon();
				eastPoly.addPoint(east.x, south.y);
				eastPoly.addPoint(rightX, south.y);
				eastPoly.addPoint(rightX, bottomY);
				return eastPoly.contains(point) ? Region.EAST : Region.SOUTH;
			}
			return Region.SOUTH;
		}

		// Now check EAST and WEST. We've already checked NORTH and SOUTH, so we
		// don't have to
		// check for NE, SE, NW, and SW anymore.
		Rectangle east = getEastRegion(component);
		if (east.contains(point)) {
			return Region.EAST;
		}
		Rectangle west = getWestRegion(component);
		if (west.contains(point)) {
			return Region.WEST;
		}

		// not in any of the outer regions, so return CENTER.
		return Region.CENTER;
	}

	/**
	 * Returns the rectangular bounds within the specified component that
	 * represent it's {@code DockingConstants.NORTH_REGION}. This method
	 * dispatches to {@code getRegionBounds(Component c, String region)},
	 * passing an argument of {@code DockingConstants.NORTH_REGION} for the
	 * region parameter. If the specified {@code Component} is {@code null},
	 * then a {@code null} reference is returned.
	 *
	 * @param component the {@code Component} whose north region is to be returned.
	 * @return the bounds containing the north region of the specified
	 * {@code Component}.
	 * @see RegionChecker#getNorthRegion(Component)
	 * @see #getRegionBounds(Component, Region)
	 */
	@Override
	public Rectangle getNorthRegion(Component component) {
		return getRegionBounds(component, Region.NORTH);
	}

	/**
	 * Returns the rectangular bounds within the specified component that
	 * represent it's {@code DockingConstants.SOUTH_REGION}. This method
	 * dispatches to {@code getRegionBounds(Component c, String region)},
	 * passing an argument of {@code DockingConstants.SOUTH_REGION} for the
	 * region parameter. If the specified {@code Component} is {@code null},
	 * then a {@code null} reference is returned.
	 *
	 * @param component the {@code Component} whose south region is to be returned.
	 * @return the bounds containing the north region of the specified
	 * {@code Component}.
	 * @see RegionChecker#getSouthRegion(Component)
	 * @see #getRegionBounds(Component, Region)
	 */
	@Override
	public Rectangle getSouthRegion(Component component) {
		return getRegionBounds(component, Region.SOUTH);
	}

	/**
	 * Returns the rectangular bounds within the specified component that
	 * represent it's {@code DockingConstants.EAST_REGION}. This method
	 * dispatches to {@code getRegionBounds(Component c, String region)},
	 * passing an argument of {@code DockingConstants.EAST_REGION} for the
	 * region parameter. If the specified {@code Component} is {@code null},
	 * then a {@code null} reference is returned.
	 *
	 * @param component the {@code Component} whose east region is to be returned.
	 * @return the bounds containing the north region of the specified
	 * {@code Component}.
	 * @see RegionChecker#getEastRegion(Component)
	 * @see #getRegionBounds(Component, Region)
	 */
	@Override
	public Rectangle getEastRegion(Component component) {
		return getRegionBounds(component, Region.EAST);
	}

	/**
	 * Returns the rectangular bounds within the specified component that
	 * represent it's {@code DockingConstants.WEST_REGION}. This method
	 * dispatches to {@code getRegionBounds(Component c, String region)},
	 * passing an argument of {@code DockingConstants.WEST_REGION} for the
	 * region parameter. If the specified {@code Component} is {@code null},
	 * then a {@code null} reference is returned.
	 *
	 * @param component the {@code Component} whose west region is to be returned.
	 * @return the bounds containing the north region of the specified
	 * {@code Component}.
	 * @see RegionChecker#getWestRegion(Component)
	 * @see #getRegionBounds(Component, Region)
	 */
	@Override
	public Rectangle getWestRegion(Component component) {
		return getRegionBounds(component, Region.WEST);
	}

	/**
	 * Returns the bounding {@code Rectangle} within the specified component
	 * that represents the specified region. If {@code c} or {@code region} are
	 * null, then this method returns a {@code null} reference.
	 * <p>
	 * This method dispatches to
	 * {@code getRegionSize(Component c, String region)} to determine the
	 * proportional size of the specified {@code Component} dedicated to the
	 * specified region. It then multiplies this value by the relevant
	 * {@code Component} dimension (<i>{@code width} for east/west,
	 * {@code height} for north/south</i>) and returns a {@code Rectangle} with
	 * the resulting dimension, spanning the {@code Component} edge for the
	 * specified region.
	 *
	 * @param component      the {@code Component} whose region bounds are to be returned.
	 * @param region the specified region that is to be examined.
	 * @return the bounds containing the supplied region of the specified
	 * {@code Component}.
	 * @see RegionChecker#getRegionBounds(Component, Region)
	 * @see #getRegionSize(Component, Region)
	 */
	@Override
	public Rectangle getRegionBounds(Component component, Region region) {
		if (component != null && region != null) {
			float size = getRegionSize(component, region);
			return calculateRegionalBounds(component, region, size);
		}
		return null;
	}

	/**
	 * Returns the bounding {@code Rectangle} within the specified component
	 * that represents the desired area to be allotted for sibling
	 * {@code Components} in the specified region. If {@code c} or
	 * {@code region} are null, then this method returns a {@code null}
	 * reference.
	 * <p>
	 * This method dispatches to
	 * {@code getSiblingSize(Component c, String region)} to determine the
	 * proportional size of the specified {@code Component} dedicated to
	 * siblings in the specified region. It then multiplies this value by the
	 * relevant {@code Component} dimension (<i>{@code width} for east/west,
	 * {@code height} for north/south</i>) and returns a {@code Rectangle} with
	 * the resulting dimension, spanning the {@code Component} edge for the
	 * specified region.
	 *
	 * @param component      the {@code Component} whose sibling bounds are to be returned.
	 * @param region the specified region that is to be examined.
	 * @return the bounds representing the allotted sibling area for the
	 * supplied region of the specified {@code Component}.
	 * @see #getSiblingSize(Component, String)
	 */
	@Override
	public Rectangle getSiblingBounds(Component component, Region region) {
		if (component != null && region != null) {
			float size = getSiblingSize(component, region);
			return calculateRegionalBounds(component, region, size);
		}
		return null;
	}

	private Rectangle calculateRegionalBounds(Component component, Region region,
											  float size) {
		if (component == null || region == null) {
			return null;
		}

		Rectangle bounds = component.getBounds();

		if (region == Region.NORTH || region == Region.SOUTH) {
			int h = Math.round(bounds.height * size);
			int y = region == Region.NORTH ? 0 : bounds.height - h;
			return new Rectangle(0, y, bounds.width, h);
		}

		if (region == Region.WEST || region == Region.EAST) {
			int w = Math.round(bounds.width * size);
			int x = region == Region.WEST ? 0 : bounds.width - w;
			return new Rectangle(x, 0, w, bounds.height);
		}
		return null;
	}

	/**
	 * Returns a percentage (0.0F through 1.0F) representing the amount of space
	 * allotted for the specified region within the specified {@code Component}.
	 * <p>
	 * This method resolves the {@code Dockable} associated with the specified
	 * {@code Component} and dispatches to
	 * {@code getRegionPreference(Dockable d, String region)}.
	 * {@code getRegionPreference(Dockable d, String region)} attempts to invoke
	 * {@code getDockingProperties()} on the {@code Dockable} to resolve a
	 * {@code DockablePropertySet} instance and return from its
	 * {@code getRegionInset(String region)} method.
	 * <p>
	 * If the specified {@code Component} is {@code null}, no {@code Dockable}
	 * can be resolved, or no value is specified in the {@code Dockable's}
	 * associated {@code DockingProps} instance, then the default value of
	 * {@code RegionChecker.DEFAULT_REGION_SIZE} is returned.
	 *
	 * @param component      the {@code Component} whose region is to be examined.
	 * @param region the specified region that is to be examined.
	 * @return the percentage of the specified {@code Component} allotted for
	 * the specified region.
	 * @see RegionChecker#getRegionSize(Component, Region)
	 * @see DockingManager#getDockable(Component)
	 * @see #getRegionPreference(Dockable, Region)
	 * @see Dockable#getDockingProperties()
	 */
	@Override
	public float getRegionSize(Component component, Region region) {
		Dockable dockable = DockingManager.getDockable(component);
		return getRegionPreference(dockable, region);
	}

	/**
	 * Returns a percentage (0.0F through 1.0F) representing the amount of space
	 * allotted for sibling {@code Component} docked to the specified region
	 * within the specified {@code Component}.
	 * <p>
	 * This method resolves the {@code Dockable} associated with the specified
	 * {@code Component} and dispatches to
	 * {@code getSiblingPreference(Dockable d, String region)}.
	 * {@code getSiblingPreference(Dockable d, String region)} attempts to
	 * invoke {@code getDockingProperties()} on the {@code Dockable} to resolve
	 * a {@code DockablePropertySet} instance and return from its
	 * {@code getSiblingSize(String region)} method.
	 * <p>
	 * If the specified {@code Component} is {@code null}, no {@code Dockable}
	 * can be resolved, or no value is specified in the {@code Dockable's}
	 * associated {@code DockingProps} instance, then the default value of
	 * {@code RegionChecker.DEFAULT_SIBLING_SIZE} is returned.
	 *
	 * @param component      the {@code Component} whose sibling size is to be examined.
	 * @param region the specified region that is to be examined.
	 * @return the percentage of the specified {@code Component} allotted for
	 * the siblings within the specified region.
	 * @see DockingManager#getDockable(Component)
	 * @see #getSiblingPreference(Dockable, String)
	 * @see Dockable#getDockingProperties()
	 */
	@Override
	public float getSiblingSize(Component component, Region region) {
		Dockable d = DockingManager.getDockable(component);
		return getSiblingPreference(d, region);
	}

	private static float getDockingInset(Float value, float defaultVal,
										 float max, float min) {
		float f = value == null ? -1 : value;
		if (f == -1) {
			f = defaultVal;
		}
		return checkBounds(f, max, min);
	}

	private static float checkBounds(float val, float max, float min) {
		return Math.max(Math.min(val, max), min);
	}

	/**
	 * Returns {@code size} if it is between the values
	 * {@code RegionChecker.MIN_REGION_SIZE} and
	 * {@code RegionChecker.MAX_REGION_SIZE}. If {@code size} is less than
	 * {@code RegionChecker.MIN_REGION_SIZE}, then
	 * {@code RegionChecker.MIN_REGION_SIZE} is returned. If {@code size} is
	 * greater than {@code RegionChecker.MAX_REGION_SIZE}, then
	 * {@code RegionChecker.MAX_REGION_SIZE} is returned.
	 *
	 * @return a valid {@code size} value between
	 * {@code RegionChecker.MIN_REGION_SIZE} and
	 * {@code RegionChecker.MAX_REGION_SIZE}, inclusive.
	 */
	private static float validateRegionSize(float size) {
		return checkBounds(size, MAX_REGION_SIZE, MIN_REGION_SIZE);
	}

	/**
	 * Returns {@code size} if it is between the values
	 * {@code RegionChecker.MIN_SIBILNG_SIZE} and
	 * {@code RegionChecker.MAX_SIBILNG_SIZE}. If {@code size} is less than
	 * {@code RegionChecker.MIN_SIBILNG_SIZE}, then
	 * {@code RegionChecker.MIN_SIBILNG_SIZE} is returned. If {@code size} is
	 * greater than {@code RegionChecker.MAX_SIBILNG_SIZE}, then
	 * {@code RegionChecker.MAX_SIBILNG_SIZE} is returned.
	 *
	 * @return a valid {@code size} value between
	 * {@code RegionChecker.MIN_SIBILNG_SIZE} and
	 * {@code RegionChecker.MAX_SIBILNG_SIZE}, inclusive.
	 */
	public static float validateSiblingSize(float size) {
		return checkBounds(size, MAX_SIBILNG_SIZE, MIN_SIBILNG_SIZE);
	}

	/**
	 * Returns a percentage (0.0F through 1.0F) representing the amount of space
	 * allotted for the specified region within the specified {@code Dockable}.
	 * <p>
	 * This method calls {@code getDockingProperties()} on the {@code Dockable}
	 * to resolve a {@code DockablePropertySet} instance. It then invokes
	 * {@code getRegionInset(String region)} on the {@code DockablePropertySet}
	 * to retrieve the preferred region size. If the {@code Dockable} is
	 * {@code null} or no region preference can be found, then the default value
	 * of {@code RegionChecker.DEFAULT_REGION_SIZE} is returned. Otherwise, the
	 * retrieved region preference is passed through
	 * {@code validateRegionSize(float size)} and returned.
	 *
	 * @param dockable      the {@code Dockable} whose region is to be checked
	 * @param region the region of the specified {@code Dockable} to be checked
	 * @return a percentage (0.0F through 1.0F) representing the amount of space
	 * allotted for the specified region within the specified
	 * {@code Dockable}.
	 * @see Dockable#getDockingProperties()
	 * @see RegionChecker#DEFAULT_REGION_SIZE
	 * @see #validateRegionSize(float)
	 */
	private static float getRegionPreference(Dockable dockable, Region region) {
		Float inset = dockable == null ? null : dockable.getDockingProperties()
				.getRegionInset(region);
		return getDockingInset(inset, DEFAULT_REGION_SIZE, MAX_REGION_SIZE,
				MIN_REGION_SIZE);
	}

	/**
	 * Returns a percentage (0.0F through 1.0F) representing the amount of space
	 * allotted for sibling {@code Components} docked to the specified region
	 * within the specified {@code Dockable}.
	 * <p>
	 * This method calls {@code getDockingProperties()} on the {@code Dockable}
	 * to resolve a {@code DockablePropertySet} instance. It then invokes
	 * {@code getSiblingSize(String region)} on the {@code DockablePropertySet}
	 * to retrieve the preferred sibling size. If the {@code Dockable} is
	 * {@code null} or no sibling preference can be found, then the default
	 * value of {@code RegionChecker.DEFAULT_SIBLING_SIZE} is returned.
	 * Otherwise, the retrieved region preference is passed through
	 * {@code validateSiblingSize(float size)} and returned.
	 *
	 * @param dockable      the {@code Dockable} whose sibling size is to be checked
	 * @param region the region of the specified {@code Dockable} to be checked
	 * @return a percentage (0.0F through 1.0F) representing the amount of space
	 * allotted for sibling {@code Components} docked to the specified
	 * region within the specified {@code Dockable}.
	 * @see Dockable#getDockingProperties()
	 * @see RegionChecker#DEFAULT_SIBLING_SIZE
	 * @see #validateSiblingSize(float)
	 */
	private static float getSiblingPreference(Dockable dockable, Region region) {
		Float size = dockable == null ? null : dockable.getDockingProperties()
				.getSiblingSize(region);
		return getDockingInset(size, DockingManager.getDefaultSiblingSize(),
				MAX_SIBILNG_SIZE, MIN_SIBILNG_SIZE);
	}
}
