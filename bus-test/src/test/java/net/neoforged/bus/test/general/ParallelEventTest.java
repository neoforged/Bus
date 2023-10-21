/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.bus.test.general;

import net.neoforged.bus.ListenerList;
import net.neoforged.bus.api.BusBuilder;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.test.ITestHandler;
import net.neoforged.bus.test.Whitebox;
import net.neoforged.bus.testjar.DummyEvent;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

public abstract class ParallelEventTest implements ITestHandler {
    private static final int BUS_COUNT = 16;
    private static final int LISTENER_COUNT = 1000;
    private static final int RUN_ITERATIONS = 1000;

    private static final AtomicLong COUNTER = new AtomicLong();

    @Override
    public void before(Supplier<BusBuilder> builder) {
        COUNTER.set(0);
    }

    protected void handle(DummyEvent.GoodEvent event) {
        COUNTER.incrementAndGet();
    }

    public static class Multiple extends ParallelEventTest {
        @Override
        public void test(Supplier<BusBuilder> builder) {
            Set<IEventBus> busSet = new HashSet<>();
            for (int i = 0; i < BUS_COUNT; i++) {
                busSet.add(builder.get().build()); //make buses for concurrent testing
            }
            busSet.parallelStream().forEach(iEventBus -> { //execute parallel listener adding
                for (int i = 0; i < LISTENER_COUNT; i++)
                    iEventBus.addListener(this::handle);
            });

            // Make sure it tracked them all
            busSet.forEach(bus -> {
                ListenerList afterAdd = Whitebox.invokeMethod(bus, "getListenerList", DummyEvent.GoodEvent.class);
                assertEquals(LISTENER_COUNT, afterAdd.getListeners().length, "Failed to register all event handlers");
            });

            busSet.parallelStream().forEach(iEventBus -> { //post events parallel
                for (int i = 0; i < RUN_ITERATIONS; i++)
                    iEventBus.post(new DummyEvent.GoodEvent());
            });

            assertEquals(COUNTER.get(), BUS_COUNT * LISTENER_COUNT * RUN_ITERATIONS);
        }
    }

    public static class Single extends ParallelEventTest {
        @Override
        public void test(Supplier<BusBuilder> builder) {
            IEventBus bus = builder.get().build();

            Set<Runnable> toAdd = new HashSet<>();

            for (int i = 0; i < LISTENER_COUNT; i++) { //prepare parallel listener adding
                toAdd.add(() -> bus.addListener(this::handle));
            }
            toAdd.parallelStream().forEach(Runnable::run); //execute parallel listener adding

            // Make sure it tracked them all
            ListenerList afterAdd = Whitebox.invokeMethod(bus, "getListenerList", DummyEvent.GoodEvent.class);
            assertEquals(LISTENER_COUNT, afterAdd.getListeners().length, "Failed to register all event handlers");

            toAdd = new HashSet<>();
            for (int i = 0; i < RUN_ITERATIONS; i++) //prepare parallel event posting
                toAdd.add(() -> bus.post(new DummyEvent.GoodEvent()));
            toAdd.parallelStream().forEach(Runnable::run); //post events parallel

            assertEquals(COUNTER.get(), LISTENER_COUNT * RUN_ITERATIONS);
        }
    }
}
