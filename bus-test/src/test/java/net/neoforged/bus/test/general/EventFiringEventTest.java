package net.neoforged.bus.test.general;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertTrue;

import net.neoforged.bus.api.BusBuilder;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.test.ITestHandler;

public class EventFiringEventTest implements ITestHandler {
    @Override
    public void test(Supplier<BusBuilder> builder) {
        IEventBus bus = builder.get().build();
        AtomicBoolean handled1 = new AtomicBoolean(false);
        AtomicBoolean handled2 = new AtomicBoolean(false);
        bus.addListener(Event1.class, (event1) -> {
            bus.post(new AbstractEvent.Event2());
            handled1.set(true);
        });
        bus.addListener(AbstractEvent.Event2.class, (event2) -> {
            handled2.set(true);
        });

        bus.post(new Event1());

        assertTrue(handled1.get(), "handled Event1");
        assertTrue(handled2.get(), "handled Event2");
    }

    public static class Event1 extends Event {}

    public static abstract class AbstractEvent extends Event {
        public static class Event2 extends AbstractEvent {}
    }
}
