/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.bus.test.general;

import net.neoforged.bus.api.BusBuilder;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.test.ITestHandler;
import net.neoforged.bus.testjar.DummyEvent;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CancelledEventTest implements ITestHandler {
    @Override
    public void test(Supplier<BusBuilder> builder) {
        IEventBus bus = builder.get().build();

        Listener listener = new Listener();
        bus.register(listener);

        DummyEvent.CancellableEvent event = new DummyEvent.CancellableEvent();
        event.setCanceled(true);
        bus.post(event);

        assertTrue(listener.receivedCanceled, "Received the canceled event");
    }

    public static class Listener {

        boolean receivedCanceled = false;

        @SubscribeEvent(receiveCanceled = true)
        public void onEvent(DummyEvent.CancellableEvent event) {
            this.receivedCanceled = true;
        }

    }
}
