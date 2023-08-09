package net.minecraftforge.eventbus.test.general;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraftforge.eventbus.api.BusBuilder;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.test.ITestHandler;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PostPhaseTest implements ITestHandler {
    public static class TestEvent extends Event {}

    @Override
    public void test(Consumer<Class<?>> validator, Supplier<BusBuilder> builder) {
        var counter = new AtomicInteger();

        var busses = List.of(builder.get().build(), builder.get().build());

        busses.get(0).addListener(EventPriority.LOW, (TestEvent event) -> assertEquals(2, counter.getAndIncrement()));
        busses.get(0).addListener((TestEvent event) -> assertEquals(0, counter.getAndIncrement()));
        busses.get(1).addListener((TestEvent event) -> assertEquals(1, counter.getAndIncrement()));

        var event = new TestEvent();
        for (var phase : EventPriority.values()) {
            event.setPhase(phase);
            for (var bus : busses) {
                bus.postPhase(phase, event);
            }
        }

        assertEquals(3, counter.get());
    }
}
