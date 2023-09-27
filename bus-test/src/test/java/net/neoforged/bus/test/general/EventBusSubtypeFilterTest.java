package net.neoforged.bus.test.general;

import java.util.function.Supplier;

import net.neoforged.bus.api.BusBuilder;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.test.ITestHandler;

import static org.junit.jupiter.api.Assertions.*;

public abstract class EventBusSubtypeFilterTest implements ITestHandler {
    public interface MarkerEvent {}
    public static class BaseEvent extends Event implements MarkerEvent {}
    public static class OtherEvent extends Event {}

    private static IEventBus bus(Supplier<BusBuilder> builder) {
        return builder.get().markerType(MarkerEvent.class).build();
    }

    private static IEventBus busCheck(Supplier<BusBuilder> builder) {
        return builder.get().markerType(MarkerEvent.class).checkTypesOnDispatch().build();
    }

    public static class Valid extends EventBusSubtypeFilterTest {
        @Override
        public void test(Supplier<BusBuilder> builder) {
            IEventBus bus = busCheck(builder);
            assertDoesNotThrow(() -> bus.addListener((BaseEvent e) -> {}));
            assertDoesNotThrow(() -> bus.post(new BaseEvent()));
        }
    }

    public static class Invalid extends EventBusSubtypeFilterTest {
        @Override
        public void test(Supplier<BusBuilder> builder) {
            IEventBus bus = busCheck(builder);
            assertThrows(IllegalArgumentException.class, () -> bus.addListener((OtherEvent e) -> {}));
            assertThrows(IllegalArgumentException.class, () -> bus.post(new OtherEvent()));
        }
    }

    public static class InvalidNoDispatch extends EventBusSubtypeFilterTest {
        @Override
        public void test(Supplier<BusBuilder> builder) {
            IEventBus bus = bus(builder);
            assertThrows(IllegalArgumentException.class, () -> bus.addListener((OtherEvent e) -> {}));
            assertDoesNotThrow(() -> bus.post(new OtherEvent()));
        }
    }
}
