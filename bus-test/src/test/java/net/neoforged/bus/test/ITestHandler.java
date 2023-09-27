package net.neoforged.bus.test;

import java.util.function.Supplier;

import net.neoforged.bus.api.BusBuilder;

public interface ITestHandler {
    default void before(Supplier<BusBuilder> builder) { before(); }
    default void before() {}
    default void after(Supplier<BusBuilder> builder) { after(); }
    default void after() {}

    void test(Supplier<BusBuilder> builder);
}