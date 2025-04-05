package net.neoforged.bus.benchmarks;

import net.neoforged.bus.api.BusBuilder;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.IEventBus;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

@State(Scope.Benchmark)
public class ListenerRebuildBenchmark {

    @Benchmark
    public int testOneThousandRebuilds() {
        final IEventBus bus = BusBuilder.builder().build();

        int i = 0;
        for (; i < 1000; i++) {
            bus.addListener(SomeEvent.class, ev -> {});
            bus.post(SomeEvent.INSTANCE);
        }
        return i;
    }

    @Benchmark
    public int testTenThousandRebuilds() {
        final IEventBus bus = BusBuilder.builder().build();

        int i = 0;
        for (; i < 10_000; i++) {
            bus.addListener(SomeEvent.class, ev -> {});
            bus.post(SomeEvent.INSTANCE);
        }
        return i;
    }

    private static final class SomeEvent extends Event {
        private static final SomeEvent INSTANCE = new SomeEvent();
    }
}
