package net.minecraftforge.eventbus.test.general;

import net.minecraftforge.eventbus.api.BusBuilder;
import net.minecraftforge.eventbus.test.ITestHandler;
import net.minecraftforge.eventbus.testjar.DummyEvent;
import net.minecraftforge.eventbus.testjar.EventBusTestClass;
import static org.junit.jupiter.api.Assertions.*;

import java.util.function.Supplier;

public class EventHandlerExceptionTest implements ITestHandler {
    @Override
    public void test(Supplier<BusBuilder> builder) {
        var bus = builder.get().build();
        var listener = new EventBusTestClass();
        bus.register(listener);
        assertThrows(RuntimeException.class, ()->bus.post(new DummyEvent.BadEvent()));
    }
}
