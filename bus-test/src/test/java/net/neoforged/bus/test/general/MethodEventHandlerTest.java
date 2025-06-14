/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.bus.test.general;

import net.neoforged.bus.api.BusBuilder;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.test.ITestHandler;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

public class MethodEventHandlerTest implements ITestHandler {
    @Override
    public void test(Supplier<BusBuilder> builder) {
        final var bus = builder.get().build();

        assertDoesNotThrow(() -> bus.register(Listener.class.getDeclaredMethod("onEvent", TestEvent.class)));

        final var event = new TestEvent();
        bus.post(event);
        assertTrue(event.wasReceived, "event received");

        assertThrows(IllegalArgumentException.class, () -> bus.register(Listener.class.getDeclaredMethod("nonStaticOnEvent", TestEvent.class)));
        assertThrows(IllegalArgumentException.class, () -> bus.register(Listener.class.getDeclaredMethod("nonAnnotatedOnEvent", TestEvent.class)));
    }

    static class TestEvent extends Event {
        boolean wasReceived;
    }

    private static class Listener {
        @SubscribeEvent
        static void onEvent(TestEvent testEvent) {
            testEvent.wasReceived = true;
        }

        @SubscribeEvent
        void nonStaticOnEvent(TestEvent testEvent) {

        }

        static void nonAnnotatedOnEvent(TestEvent testEvent) {

        }
    }
}
