package net.neoforged.bus.benchmarks;

import net.neoforged.bus.api.*;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

@State(Scope.Benchmark)
public class EventBusStressTest {
    private static final IEventBus CL_BUS = BusBuilder.builder().factoryType(FactoryType.CLASS_LOADER).build();
    private static final IEventBus LMF_BUS = BusBuilder.builder().factoryType(FactoryType.LAMBDA_META_FACTORY).build();
    private static final IEventBus MH_BUS = BusBuilder.builder().factoryType(FactoryType.METHOD_HANDLES).build();

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
            if (evt.y == 2) {
                throw new RuntimeException("sad!");
            }
        }

        @SubscribeEvent
        public void onEvent(TestCancellableEvent evt) {
            if (evt.x == lookingFor) {
                evt.y++;
            }
            if (evt.y == 2) {
                throw new RuntimeException("sad!");
            }
        }
    }

    @Setup
    public void setup() {
        for (int i = 0; i < 10; ++i) {
            for (int j = 0; j < 10; ++j) {
                CL_BUS.register(new Listeners(j));
                LMF_BUS.register(new Listeners(j));
                MH_BUS.register(new Listeners(j));
            }
        }
    }

    @Benchmark
    public int testClassLoader() {
        return CL_BUS.post(new TestEvent()).y;
    }

    @Benchmark
    public int testClassLoaderCancellable() {
        return CL_BUS.post(new TestCancellableEvent()).y;
    }

    @Benchmark
    public int testLambdaMetaFactory() {
        return LMF_BUS.post(new TestEvent()).y;
    }

    @Benchmark
    public int testLambdaMetaFactoryCancellable() {
        return LMF_BUS.post(new TestCancellableEvent()).y;
    }

    @Benchmark
    public int testMethodHandles() {
        return MH_BUS.post(new TestEvent()).y;
    }

    @Benchmark
    public int testMethodHandlesCancellable() {
        return MH_BUS.post(new TestCancellableEvent()).y;
    }
}
