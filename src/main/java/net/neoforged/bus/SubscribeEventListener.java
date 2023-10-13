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
class SubscribeEventListener implements IEventListener, IWrapperListener {
    private final IEventListener handler;
    private final SubscribeEvent subInfo;
    private final boolean isGeneric;
    private String readable;
    private Type filter = null;

    public SubscribeEventListener(Object target, Method method, boolean isGeneric) {
        handler = EventListenerFactory.create(method, target);

        subInfo = method.getAnnotation(SubscribeEvent.class);
        readable = "@SubscribeEvent: " + target + " " + method.getName() + getMethodDescriptor(method);
        this.isGeneric = isGeneric;
        if (isGeneric)
        {
            Type type = method.getGenericParameterTypes()[0];
            if (type instanceof ParameterizedType)
            {
                filter = ((ParameterizedType)type).getActualTypeArguments()[0];
                if (filter instanceof ParameterizedType) // Unlikely that nested generics will ever be relevant for event filtering, so discard them
                {
                    filter = ((ParameterizedType)filter).getRawType();
                }
                else if (filter instanceof WildcardType)
                {
                    // If there's a wildcard filter of Object.class, then remove the filter.
                    final WildcardType wfilter = (WildcardType) filter;
                    if (wfilter.getUpperBounds().length == 1 && wfilter.getUpperBounds()[0] == Object.class && wfilter.getLowerBounds().length == 0) {
                        filter = null;
                    }
                }
            }
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void invoke(Event event)
    {
        if (handler != null)
        {
            if (isGeneric)
            {
                if (filter == null || filter == ((IGenericEvent)event).getGenericType()) {
                    if (!(event instanceof ICancellableEvent cancellableEvent) || !cancellableEvent.isCanceled()) {
                        handler.invoke(event);
                    }
                }
            } else {
                // The cast is safe because the check is removed if the event is not cancellable
                if (!((ICancellableEvent) event).isCanceled()) {
                    handler.invoke(event);
                }
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
    public IEventListener getWithoutCheck() {
        return handler;
    }
}
