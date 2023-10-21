/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.bus.test.general;

import net.neoforged.bus.api.BusBuilder;
import net.neoforged.bus.test.ITestHandler;
import net.neoforged.bus.testjar.DummyEvent;
import net.neoforged.bus.testjar.EventBusTestClass;
import static org.junit.jupiter.api.Assertions.*;

import java.util.function.Supplier;

public class EventHandlerExceptionTest implements ITestHandler {
    @Override
    public void test(Supplier<BusBuilder> builder) {
        var bus = builder.get().build();
        var listener = new EventBusTestClass();
        bus.register(listener);
        assertThrows(RuntimeException.class, ()->bus.post(new DummyEvent.BadEvent()));
    }
}
