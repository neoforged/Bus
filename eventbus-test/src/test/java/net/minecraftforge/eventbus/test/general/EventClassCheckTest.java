package net.minecraftforge.eventbus.test.general;

import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraftforge.eventbus.api.BusBuilder;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.test.ITestHandler;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EventClassCheckTest implements ITestHandler {
    private interface IModBusEvent {}

    public static class TestModBusEvent extends Event implements IModBusEvent {}
    public static class TestForgeBusEvent extends Event {}

    @Override
    public void test(Consumer<Class<?>> validator, Supplier<BusBuilder> builder) {
        var modBus = builder.get().markerType(IModBusEvent.class).build();
        var forgeBus = builder.get().eventClassFilter(
                eventClass -> !IModBusEvent.class.isAssignableFrom(eventClass),
                eventClass -> "The Forge bus does not accept IModBusEvent subclasses, which " + eventClass + " is. Post it on the mod event bus instead."
        ).build();

        assertDoesNotThrow(() -> modBus.addListener((TestModBusEvent event) -> {}));
        assertDoesNotThrow(() -> forgeBus.addListener((TestForgeBusEvent event) -> {}));
        assertThrows(IllegalArgumentException.class, () -> modBus.addListener((TestForgeBusEvent event) -> {}));
        assertThrows(IllegalArgumentException.class, () -> forgeBus.addListener((TestModBusEvent event) -> {}));
    }
}
