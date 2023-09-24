package net.neoforged.bus.test.general;

import net.neoforged.bus.api.BusBuilder;
import net.neoforged.bus.test.ITestHandler;
import net.neoforged.bus.testjar.DummyEvent;
import net.neoforged.bus.testjar.EventBusTestClass;
import static org.junit.jupiter.api.Assertions.*;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ParentHandlersGetInvokedTestDummy implements ITestHandler {
    @Override
    public void test(Consumer<Class<?>> validator, Supplier<BusBuilder> builder) {
        var bus = builder.get().build();
        var listener = new EventBusTestClass();
        bus.register(listener);
        bus.post(new DummyEvent.GoodEvent());
        assertTrue(listener.HIT1, "DummyEvent handler did not fire");
        assertTrue(listener.HIT2, "GoodEvent handler did not fire");
    }
}
