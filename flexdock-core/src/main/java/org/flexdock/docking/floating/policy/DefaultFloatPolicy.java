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
package org.flexdock.docking.floating.policy;

import org.flexdock.docking.Dockable;
import org.flexdock.docking.DockingPort;
import org.flexdock.docking.event.DockingEvent;
import org.flexdock.docking.floating.frames.FloatingDockingPort;
import org.flexdock.docking.floating.policy.FloatPolicy.NullFloatPolicy;

import java.awt.*;
import java.util.Set;

/**
 * This class provides an implementation of the {@code FloatPolicy} interface to
 * provide default behavior for the framework. It blocks floating operations for
 * {@code Dockables} without any frame drag sources, for already floating
 * {@code Dockables} that cannot be reparented within a new dialog, or if global
 * floating support has been disabled.
 *
 * @author Christopher Butler
 */
public class DefaultFloatPolicy extends NullFloatPolicy {

    private static final DefaultFloatPolicy SINGLETON = new DefaultFloatPolicy();

    /**
     * Returns a singleton instance of {@code DefaultFloatPolicy}.
     *
     * @return a singleton instance of {@code DefaultFloatPolicy}.
     */
    public static DefaultFloatPolicy getInstance() {
        return SINGLETON;
    }

    /**
     * Checks the previous {@code DockingPort} for the specified
     * {@code DockingEvent} and returns {@code false} if it is in a floating
     * dialog and contains less than two {@code Dockables}. A floating dialog
     * may contain multiple {@code Dockables}, each of which may be dragged out
     * of the current dialog to float in their own dialog. However, if a
     * floating dialog only contains a single {@code Dockable}, it makes no
     * sense to remove the {@code Dockable} only to float it within another
     * dialog. This situation is caught by this method and the docking operation
     * is blocked.
     *
     * @param evt the {@code DockingEvent} to be checked for drop-to-float
     *            support
     * @return {@code false} if the {@code DockingEvent} is attempting to float
     * an already floating {@code Dockable} with no other
     * {@code Dockables} in its current dialog; {@code true} otherwise.
     * @see FloatPolicy#isFloatDropAllowed(DockingEvent)
     * @see DockingEvent#getOldDockingPort()
     * @see FloatingDockingPort#getDockableCount()
     * @see DockingEvent#consume()
     */
    @Override
    public boolean isFloatDropAllowed(DockingEvent evt) {
        DockingPort oldPort = evt.getOldDockingPort();
        // if we're already floating, and we're the only dockable
        // in a floating dockingport, then we don't want to undock
        // from the port and re-float (dispose and create a new DockingFrame).
        if (oldPort instanceof FloatingDockingPort) {
            FloatingDockingPort dockingPort = (FloatingDockingPort) oldPort;
            if (dockingPort.getDockableCount() < 2) {
                evt.consume();
                return false;
            }
        }

        return super.isFloatDropAllowed(evt);
    }

    /**
     * Blocks floating support (returns false) if {@code dockable} is
     * {@code null}, if {@code FloatPolicyManager.isGlobalFloatingEnabled()}
     * returns {@code false}, or if there are no entries within the {@code Set}
     * returned by {@code dockable.getFrameDragSources()}. Otherwise, this
     * method returns {@code true}.
     *
     * @param dockable the {@code Dockable} to be checked for floating support
     * @return {@code false} if floating is blocked for the specified
     * {@code Dockable}; {@code true} otherwise.
     * @see Dockable#getFrameDragSources()
     * @see FloatPolicyManager#isGlobalFloatingEnabled()
     */
    @Override
    public boolean isFloatingAllowed(Dockable dockable) {
        if (dockable == null || !FloatPolicyManager.isGlobalFloatingEnabled()) {
            return false;
        }

        Set<Component> frameDragSources = dockable.getFrameDragSources();
        return frameDragSources != null && !frameDragSources.isEmpty() && super.isFloatingAllowed(dockable);
    }
}
