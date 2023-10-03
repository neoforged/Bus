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

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.Event.HasResult;

import java.lang.annotation.Annotation;

public class EventListenerHelper
{
    private static final LockHelper<Class<?>, Boolean> hasResult = LockHelper.withIdentityHashMap();

    public static boolean hasResult(Class<?> eventClass) {
        return hasAnnotation(eventClass, HasResult.class, hasResult);
    }

    private static boolean hasAnnotation(Class<?> eventClass, Class<? extends Annotation> annotation, LockHelper<Class<?>, Boolean> lock) {
        if (eventClass == Event.class)
            return false;

        // Skip allocating lambda if possible
        var result = lock.get(eventClass);
        if (result != null)
            return result;

        return lock.computeIfAbsent(eventClass, () -> {
            var parent = eventClass.getSuperclass();
            return eventClass.isAnnotationPresent(annotation) || (parent != null && hasAnnotation(parent, annotation, lock));
        });
    }
}
