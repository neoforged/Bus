/*
 * Minecraft Forge
 * Copyright (c) 2016.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package net.neoforged.bus.api;

import net.neoforged.bus.EventListenerHelper;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Base Event class that all other events are derived from.
 *
 * <h3>Note on abstract events</h3>
 * Listeners cannot be registered to an abstract event class.
 * <p>
 * This is useful for classes that extend {@link Event} with more data and methods,
 * but should never be listened to directly.
 * <p>
 * For example, an event with {@code Pre} and {@code Post} subclasses might want to
 * be declared as {@code abstract} to prevent user accidentally listening to both.
 * <p>
 * All the parents of abstract event classes until {@link Event} must also be abstract.
 */
public abstract class Event
{
    /**
     * @deprecated Use a custom type and custom methods appropriate for your event,
     *             instead of the overly general {@link Event.Result} enum.
     *             {@link Event#getResult()} and {@link Event#setResult(Result)}
     *             will be removed in a future version.
     */
    @Deprecated(forRemoval = true)
    @Retention(value = RUNTIME)
    @Target(value = TYPE)
    public @interface HasResult{}

    public enum Result
    {
        DENY,
        DEFAULT,
        ALLOW
    }

    boolean isCanceled = false;
    private Result result = Result.DEFAULT;

    protected Event() { }

    /**
     * Determines if this event expects a significant result value.
     */
    public final boolean hasResult()
    {
        return EventListenerHelper.hasResult(this.getClass());
    }

    /**
     * Returns the value set as the result of this event
     */
    public final Result getResult()
    {
        return result;
    }

    /**
     * Sets the result value for this event, not all events can have a result set, and any attempt to
     * set a result for a event that isn't expecting it will result in a IllegalArgumentException.
     *
     * The functionality of setting the result is defined on a per-event bases.
     *
     * @param value The new result
     */
    public void setResult(Result value)
    {
        if (!hasResult())
        {
            throw new UnsupportedOperationException(
                    "Attempted to call Event#setResult() on an event without result of type: "
                            + this.getClass().getCanonicalName()
            );
        }
        result = value;
    }
}
