/*
 * Minecraft Forge
 * Copyright (c) 2016.
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */

package net.neoforged.bus.api;

/**
 * Base Event class that all other events are derived from.
 * <br>
 * <strong>Note on abstract events</strong>
 * <br>
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
public abstract class Event {
    boolean isCanceled = false;

    protected Event() {}
}
