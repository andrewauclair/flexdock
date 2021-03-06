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
package org.flexdock.docking;

import java.awt.*;

/**
 * This interface is designed to provide an API for allowing the
 * {@code DockingManager} to obtain {@code Dockable} instances on the fly. It
 * has a single method, {@code getDockableComponent(String dockableId)},
 * responsible for returning {@code Component} instances, possibly creating and
 * registering {@code Dockables} in the process.
 * <p>
 * Implementations of this interface will be application-specific and may be
 * plugged into the {@code DockingManager} via the call
 * {@code DockingManager.setDockableFactory(myFactory)}. Throughout the
 * framework, FlexDock makes many calls to
 * {@code DockingManager.getDockable(String id)} under the assumption that at
 * some point, the requested {@code Dockable} instance has been registered via
 * {@code DockingManager.registerDockable(Dockable dockable)}.
 * <p>
 * In the event that a {@code Dockable} with the specified ID has never been
 * formally registered, the {@code DockingManager} will check for a factory via
 * {@code DockingManager.getDockableFactory()}. If a factory is present, its
 * {@code getDockableComponent()} method is invoked. If a valid
 * {@code Component} is returned from {@code getDockableComponent()}, the
 * DockingManager will attempt to register it as a {@code Dockable} and return
 * the {@code Dockable}.
 * <p>
 * {@code DockableFactory} implementations are especially useful for
 * applications with persisted layouts where the {@code Dockables} required
 * during a layout restoration may be constructed automatically on demand by the
 * framework.
 *
 * @author Christopher Butler
 */
public interface DockableFactory {

	/**
	 * Returns a {@code Component} for the specified Dockable ID, possibly
	 * creating and registering a {@code Dockable} in the process.
	 *
	 * @param dockableId the ID for the requested dockable {@code Component}
	 * @return the {@code Component} for the specified ID
	 */
	<T extends Component & DockingStub> T getDockableComponent(String dockableId);

	/**
	 * Returns a {@code Dockable} for the specified Dockable ID, possibly
	 * creating and registering it in the process.
	 *
	 * @param dockableId the ID for the requested {@code Dockable}
	 * @return the {@code Dockable} for the specified ID
	 */
	Dockable getDockable(String dockableId);

	/**
	 * An empty implementation of {@code DockableFactory}.
	 */
	class Stub implements DockableFactory {

		/**
		 * {@inheritDoc}
		 *
		 * @return {@code null}.
		 */
		@Override
		public Dockable getDockable(String dockableId) {
			return null;
		}

		/**
		 * {@inheritDoc}
		 *
		 * @return {@code null}.
		 */
		@Override
		public <T extends Component & DockingStub> T getDockableComponent(String dockableId) {
			return null;
		}
	}
}
