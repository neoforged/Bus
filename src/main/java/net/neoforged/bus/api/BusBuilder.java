/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.bus.api;

import net.neoforged.bus.BusBuilderImpl;

/**
 * Class used to build {@link IEventBus event buses}.
 */
public interface BusBuilder {
    /**
     * {@return a new bus builder}
     */
    static BusBuilder builder() {
        return new BusBuilderImpl();
    }

    /**
     * Override the default exception handler.
     *
     * @param handler the custom exception handler to use
     * @return the builder instance
     */
    BusBuilder setExceptionHandler(IEventExceptionHandler handler);

    /**
     * Create the bus in a shut down state.
     * <p>
     * This bus will not post events (and will instead silently ignore them) until it is {@link IEventBus#start() started}.
     *
     * @return the builder instance
     */
    BusBuilder startShutdown();

    /**
     * Make the bus check posted events using the {@link #classChecker(IEventClassChecker) class checker}.
     *
     * @return the builder instance
     */
    BusBuilder checkTypesOnDispatch();

    /**
     * Helper for {@link #classChecker(IEventClassChecker)} that will check if the event type is of the given {@code markerInterface}.
     *
     * @param markerInterface a base marker interface for all events that this bus should be able to handle
     * @return the builder instance
     */
    default BusBuilder markerType(Class<?> markerInterface) {
        if (!markerInterface.isInterface()) throw new IllegalArgumentException("Cannot specify a class marker type");
        return classChecker(eventType -> {
            if (!markerInterface.isAssignableFrom(eventType)) {
                throw new IllegalArgumentException("This bus only accepts subclasses of " + markerInterface + ", which " + eventType + " is not.");
            }
        });
    }

    /**
     * Set a class checker that will allow validating the event types that the bus can handle.
     *
     * @param checker the custom checker
     * @return the builder instance
     */
    BusBuilder classChecker(IEventClassChecker checker);

    /**
     * Allow calling {@link IEventBus#post(EventPriority, Event)}.
     */
    BusBuilder allowPerPhasePost();

    /**
     * {@return a built bus configured according to the configuration of this builder}
     */
    IEventBus build();
}
