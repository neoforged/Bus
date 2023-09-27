package net.neoforged.bus.test.general;

import net.neoforged.bus.api.BusBuilder;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.test.ITestHandler;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AbstractEventListenerTest implements ITestHandler {
    public void test(Supplier<BusBuilder> builder) {
        IEventBus bus = builder.get().build();
        AtomicBoolean abstractSuperEventHandled = new AtomicBoolean(false);
        AtomicBoolean concreteSuperEventHandled = new AtomicBoolean(false);
        AtomicBoolean abstractSubEventHandled = new AtomicBoolean(false);
        AtomicBoolean concreteSubEventHandled = new AtomicBoolean(false);
        bus.addListener(AbstractSuperEvent.class, (event) -> abstractSuperEventHandled.set(true));
        bus.addListener(ConcreteSuperEvent.class, (event) -> concreteSuperEventHandled.set(true));
        bus.addListener(AbstractSubEvent.class, (event) -> abstractSubEventHandled.set(true));
        bus.addListener(ConcreteSubEvent.class, (event) -> concreteSubEventHandled.set(true));

        bus.post(new ConcreteSubEvent());

        assertTrue(abstractSuperEventHandled.get(), "handled abstract super event");
        assertTrue(concreteSuperEventHandled.get(), "handled concrete super event");
        assertTrue(abstractSubEventHandled.get(), "handled abstract sub event");
        assertTrue(concreteSubEventHandled.get(), "handled concrete sub event");
    }

    public static abstract class AbstractSuperEvent extends Event {}

    public static class ConcreteSuperEvent extends AbstractSuperEvent {
        public ConcreteSuperEvent() {}
    }

    public static class AbstractSubEvent extends ConcreteSuperEvent {
    }

    public static class ConcreteSubEvent extends AbstractSubEvent {
        public ConcreteSubEvent() {}
    }
}
