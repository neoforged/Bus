package net.neoforged.bus.test.general;

import java.util.function.Supplier;

import net.neoforged.bus.api.BusBuilder;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.test.ITestHandler;

import static org.junit.jupiter.api.Assertions.*;

public class NonPublicEventHandler implements ITestHandler {
    private static boolean hit = false;

    @Override
    public void test(Supplier<BusBuilder> builder) {
        var bus = builder.get().build();
        testCall(bus, new PUBLIC(), "PUBLIC");
        testCall(bus, new PROTECTED(), "PROTECTED");
        testCall(bus, new DEFAULT(), "DEFAULT");
        testCall(bus, new PRIVATE(), "PRIVATE");
    }

    private void testCall(IEventBus bus, Object listenerObject, String name) {
        // Register
        assertDoesNotThrow(() -> bus.register(listenerObject));
        // Post
        hit = false;
        bus.post(new Event());
        assertTrue(hit, name + " did not behave correctly: failed to hit");
        // Unregister
        bus.unregister(listenerObject);
        // Make sure we could unregister
        hit = false;
        bus.post(new Event());
        assertFalse(hit, name + " did not behave correctly: failed to unregister");
    }

    public static class PUBLIC {
        @SubscribeEvent
        public void handler(Event e) {
            hit = true;
        }
    }
    public static class PROTECTED {
        @SubscribeEvent
        protected void handler(Event e) {
            hit = true;
        }
    }
    public static class DEFAULT {
        @SubscribeEvent
        void handler(Event e) {
            hit = true;
        }
    }
    public static class PRIVATE {
        @SubscribeEvent
        private void handler(Event e) {
            hit = true;
        }
    }
}
