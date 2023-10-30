/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.bus.test.general;

import net.neoforged.bus.api.BusBuilder;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.test.ITestHandler;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

public class AbstractEventClassesTest implements ITestHandler {
    @Override
    public void test(Supplier<BusBuilder> builder) {
        var bus = builder.get().build();

        // Can't register to a broken chain
        assertThrows(Exception.class, () -> bus.addListener(Chain.Leaf.class, event -> {}));
        assertThrows(Exception.class, () -> bus.addListener(Chain.Child.class, event -> {}));

        // Can't fire broken chain either
        assertThrows(Exception.class, () -> bus.post(new Chain.Leaf()));

        // Can't register to abstract event
        assertThrows(Exception.class, () -> bus.addListener(ProperAbstract.class, event -> {}));

        // Can register to proper event and fire it
        AtomicInteger hit = new AtomicInteger(0);
        bus.addListener(ProperEvent.class, event -> hit.incrementAndGet());
        bus.post(new ProperEvent());
        assertEquals(1, hit.get());
    }

    /**
     * Missing abstract at the root.
     */
    public static class Chain {
        public static class Root extends Event {}

        public static abstract class Child extends Root {}

        public static class Leaf extends Child {}
    }

    public static abstract class ProperAbstract extends Event {}

    public static class ProperEvent extends ProperAbstract {}
}
