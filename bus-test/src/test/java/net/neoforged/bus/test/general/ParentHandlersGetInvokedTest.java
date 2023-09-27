package net.neoforged.bus.test.general;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertTrue;

import net.neoforged.bus.api.BusBuilder;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.test.ITestHandler;

public class ParentHandlersGetInvokedTest implements ITestHandler {
    @Override
    public void test(Supplier<BusBuilder> builder) {
        IEventBus bus = builder.get().build();
        AtomicBoolean superEventHandled = new AtomicBoolean(false);
        AtomicBoolean subEventHandled = new AtomicBoolean(false);
        bus.addListener(SuperEvent.class, (event) -> {
            Class<? extends SuperEvent> eventClass = event.getClass();
            if (eventClass == SuperEvent.class) {
                superEventHandled.set(true);
            } else if (eventClass == SubEvent.class) {
                subEventHandled.set(true);
            }
        });

        bus.post(new SuperEvent());
        bus.post(new SubEvent());

        assertTrue(superEventHandled.get(), "Handler was not invoked for SuperEvent");
        assertTrue(subEventHandled.get(), "Handler was not invoked for SubEvent");
    }

    public static class SuperEvent extends Event {}
    public static class SubEvent extends SuperEvent {}
}
