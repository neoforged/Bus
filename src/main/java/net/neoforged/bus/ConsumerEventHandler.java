/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.bus;

import java.util.function.Consumer;
import java.util.function.Predicate;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventListener;
import org.jetbrains.annotations.ApiStatus;

/**
 * Wraps a consumer to be used as an event handler, and overrides {@link #toString()} for better debugging.
 */
@ApiStatus.Internal
public sealed class ConsumerEventHandler extends EventListener {
    protected final Consumer<Event> consumer;

    public ConsumerEventHandler(Consumer<Event> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void invoke(Event event) {
        consumer.accept(event);
    }

    @Override
    public String toString() {
        return consumer.toString();
    }

    public static final class WithPredicate extends ConsumerEventHandler implements IWrapperListener {
        private final Predicate<Event> predicate;
        private final EventListener withoutCheck;

        public WithPredicate(Consumer<Event> consumer, Predicate<Event> predicate) {
            super(consumer);
            this.predicate = predicate;
            this.withoutCheck = new ConsumerEventHandler(consumer);
        }

        @Override
        public void invoke(Event event) {
            if (predicate.test(event)) {
                consumer.accept(event);
            }
        }

        @Override
        public EventListener getWithoutCheck() {
            return withoutCheck;
        }
    }
}
