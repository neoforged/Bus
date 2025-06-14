/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.bus.api;

/**
 * Interface used to check whether specific {@link Event} class can be posted via a {@link IEventBus}.
 */
@FunctionalInterface
public interface IEventClassChecker {
    /**
     * If the event class should be accepted for the bus, does nothing.
     * If it should not be accepted, throws an {@link IllegalArgumentException}.
     *
     * @param eventClass The type of the event to check for validity
     *
     * @throws IllegalArgumentException If the event class is not valid.
     */
    void check(Class<? extends Event> eventClass) throws IllegalArgumentException;
}
