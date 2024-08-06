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

package net.neoforged.bus;

import net.neoforged.bus.api.*;

import java.lang.reflect.*;
import static org.objectweb.asm.Type.getMethodDescriptor;

/**
 * Wrapper around an event handler generated for a {@link SubscribeEvent} method.
 */
public final class SubscribeEventListener extends EventListener implements IWrapperListener {
    private final EventListener handler;
    private final SubscribeEvent subInfo;
    private String readable;

    public SubscribeEventListener(Object target, Method method) {
        handler = EventListenerFactory.create(method, target);

        subInfo = method.getAnnotation(SubscribeEvent.class);
        readable = "@SubscribeEvent: " + target + " " + method.getName() + getMethodDescriptor(method);
    }

    @Override
    public void invoke(Event event)
    {
        if (handler != null)
        {
            // The cast is safe because the check is removed if the event is not cancellable
            if (subInfo.receiveCanceled() || !((ICancellableEvent) event).isCanceled()) {
                handler.invoke(event);
            }
        }
    }

    public EventPriority getPriority()
    {
        return subInfo.priority();
    }

    public String toString()
    {
        return readable;
    }

    @Override
    public EventListener getWithoutCheck() {
        return handler;
    }
}
