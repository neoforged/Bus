/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.bus.test.general;

import net.neoforged.bus.api.BusBuilder;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.test.ITestHandler;
import net.neoforged.bus.testjar.DummyEvent;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests that we prevent common {@link SubscribeEvent} user mistakes.
 */
public class CommonSubscribeEventErrorsTest implements ITestHandler {
    @Override
    public void test(Supplier<BusBuilder> builder) {
        var bus = builder.get().build();

        assertThrows(IllegalArgumentException.class, () -> bus.register(new HasSuperClass()));
        assertThrows(IllegalArgumentException.class, () -> bus.register(HasSuperClass.class));
        assertThrows(IllegalArgumentException.class, () -> bus.register(new HasSuperInterface()));
        assertThrows(IllegalArgumentException.class, () -> bus.register(HasSuperInterface.class));
        assertThrows(IllegalArgumentException.class, () -> bus.register(new HasNoSubscriptions()));
        assertThrows(IllegalArgumentException.class, () -> bus.register(HasNoSubscriptions.class));
        assertThrows(IllegalArgumentException.class, () -> bus.register(new HasStaticMethod()));
        assertThrows(IllegalArgumentException.class, () -> bus.register(HasInstanceMethod.class));
    }

    public static class SuperClass {
        @SubscribeEvent
        public void listenerMethod(DummyEvent event) {}
    }
    public static class HasSuperClass extends SuperClass {}

    public interface SuperInterface {
        @SubscribeEvent
        default void listenerMethod(DummyEvent event) {}
    }
    public static class HasSuperInterface implements SuperInterface {}

    public static class HasNoSubscriptions {
        public void otherMethod() {}
    }

    public static class HasStaticMethod {
        @SubscribeEvent
        public static void listenerMethod(DummyEvent event) {}
    }

    public static class HasInstanceMethod {
        @SubscribeEvent
        public void listenerMethod(DummyEvent event) {}
    }
}
