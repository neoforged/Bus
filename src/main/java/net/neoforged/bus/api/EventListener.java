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

import net.neoforged.bus.ConsumerEventHandler;
import net.neoforged.bus.GeneratedEventListener;
import net.neoforged.bus.IWrapperListener;
import net.neoforged.bus.SubscribeEventListener;

/**
 * Event listeners are wrapped with implementations of this class.
 */
public abstract sealed class EventListener implements IWrapperListener
        permits ConsumerEventHandler, GeneratedEventListener, SubscribeEventListener {
    public abstract void invoke(Event event);

    @Override
    public EventListener getWithoutCheck() {
        return null;
    }
}
