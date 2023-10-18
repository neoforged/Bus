package net.neoforged.bus.benchmarks;

import net.neoforged.bus.api.*;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

@State(Scope.Benchmark)
public class ManyBussesBenchmark {
    private static final int BUS_COUNT = 100;

    private static class TestEvent extends Event {
        int value = 0;
    }

    private final IEventBus[] busses = new IEventBus[BUS_COUNT];

    @Setup
    public void setup() {
        // Imitate FML-like setup where most mod event busses have few listeners for a given event.
        for (int i = 0; i < BUS_COUNT; ++i) {
            busses[i] = BusBuilder.builder().build();
            if (i % 5 == 0) {
                busses[i].addListener(TestEvent.class, event -> event.value++);
            }
            if (i % 7 == 0) {
                busses[i].addListener(EventPriority.HIGH, TestEvent.class, event -> event.value++);
            }
            if (i % 9 == 0) {
                busses[i].addListener(EventPriority.LOW, TestEvent.class, event -> event.value++);
            }
        }
    }

    @Benchmark
    public int testManyBusses() {
        TestEvent testEvent = new TestEvent();
        for (IEventBus bus : busses) {
            bus.post(testEvent);
        }
        return testEvent.value;
    }

    /**
     * This test respects listener priority across busses, {@link #testManyBusses()} does not.
     */
    @Benchmark
    public int testManyBussesPerPhase() {
        TestEvent testEvent = new TestEvent();
        for (EventPriority phase : EventPriority.values()) {
            for (IEventBus bus : busses) {
                bus.post(phase, testEvent);
            }
        }
        return testEvent.value;
    }
}
