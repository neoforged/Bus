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
 * Different priorities for {@link Event} listeners.
 *
 * {@link #NORMAL} is the default level for a listener registered without a priority.
 *
 * @see SubscribeEvent#priority()
 */
public enum EventPriority {
    /**
     * Priority of event listeners, listeners will be sorted with respect to this priority level.
     *
     * Note:
     * Due to using a ArrayList in the ListenerList,
     * these need to stay in a contiguous index starting at 0. {Default ordinal}
     */
    HIGHEST, //First to execute
    HIGH,
    NORMAL,
    LOW,
    LOWEST; //Last to execute
}
