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

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation to subscribe a method to an {@link Event}
 * <p>
 * This annotation can only be applied to single parameter methods, where the single parameter is a subclass of
 * {@link Event}.
 * <p>
 * Use {@link IEventBus#register(Object)} to submit either an Object instance or a {@link Class} to the event bus
 * for scanning to generate callback {@link EventListener} wrappers.
 * <p>
 * The Event Bus system generates an ASM wrapper that dispatches to the marked method.
 */
@Retention(value = RUNTIME)
@Target(value = METHOD)
public @interface SubscribeEvent {
    /**
     * {@return the priority to subscribe this listener with}
     */
    EventPriority priority() default EventPriority.NORMAL;

    /**
     * {@return whether this listener should receive cancelled events}
     */
    boolean receiveCanceled() default false;
}
