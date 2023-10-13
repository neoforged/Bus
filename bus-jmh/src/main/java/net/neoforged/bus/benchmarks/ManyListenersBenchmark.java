package net.neoforged.bus.benchmarks;

import net.neoforged.bus.api.*;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

@State(Scope.Benchmark)
public class ManyListenersBenchmark {
    private static final IEventBus BUS = BusBuilder.builder().build();

    public static class TestEvent extends Event {
        int x = 0;
        int y = 0;
    }

    public static class TestCancellableEvent extends Event implements ICancellableEvent {
        int x = 0;
        int y = 0;
    }

    public static class Listeners {
        private final int lookingFor;

        public Listeners(int lookingFor) {
            this.lookingFor = lookingFor;
        }

        @SubscribeEvent
        public void onEvent(TestEvent evt) {
            if (evt.x == lookingFor) {
                evt.y++;
            }
        }

        @SubscribeEvent
        public void onEvent(TestCancellableEvent evt) {
            if (evt.x == lookingFor) {
                evt.y++;
            }
        }
    }

    @Setup
    public void setup() {
        for (int i = 0; i < 10; ++i) {
            for (int j = 0; j < 10; ++j) {
                BUS.register(new Listeners(j));
            }
        }
    }

    @Benchmark
    public int testHundredListeners() {
        return BUS.post(new TestEvent()).y;
    }

    @Benchmark
    public int testHundredListenersCancellable() {
        return BUS.post(new TestCancellableEvent()).y;
    }
}
