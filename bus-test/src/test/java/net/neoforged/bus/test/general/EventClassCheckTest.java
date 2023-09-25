package net.neoforged.bus.test.general;

import java.util.function.Consumer;
import java.util.function.Supplier;

import net.neoforged.bus.api.BusBuilder;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.test.ITestHandler;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EventClassCheckTest implements ITestHandler {
    private interface IModBusEvent {}

    public static class TestModBusEvent extends Event implements IModBusEvent {}
    public static class TestForgeBusEvent extends Event {}

    @Override
    public void test(Consumer<Class<?>> validator, Supplier<BusBuilder> builder) {
        var modBus = builder.get().markerType(IModBusEvent.class).checkTypesOnDispatch().build();
        var forgeBus = builder.get().checkTypesOnDispatch().classChecker(eventClass -> {
            if (IModBusEvent.class.isAssignableFrom(eventClass)) {
                throw new IllegalArgumentException(
                        "The Forge bus does not accept IModBusEvent subclasses, which " + eventClass + " is. Post it on the mod event bus instead.");
            }
        }).build();

        assertDoesNotThrow(() -> modBus.addListener((TestModBusEvent event) -> {}));
        assertDoesNotThrow(() -> forgeBus.addListener((TestForgeBusEvent event) -> {}));
        assertThrows(IllegalArgumentException.class, () -> modBus.addListener((TestForgeBusEvent event) -> {}));
        assertThrows(IllegalArgumentException.class, () -> forgeBus.addListener((TestModBusEvent event) -> {}));

        assertDoesNotThrow(() -> modBus.post(new TestModBusEvent()));
        assertDoesNotThrow(() -> forgeBus.post(new TestForgeBusEvent()));
        assertThrows(IllegalArgumentException.class, () -> modBus.post(new TestForgeBusEvent()));
        assertThrows(IllegalArgumentException.class, () -> forgeBus.post(new TestModBusEvent()));
    }
}
