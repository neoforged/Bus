package net.neoforged.bus.test;

import net.neoforged.bus.api.BusBuilder;

public class TestNoLoaderBase {
    private void validate(Class<?> clazz) {
    }

    private BusBuilder builder() {
        return BusBuilder.builder();
    }

    protected void doTest(ITestHandler handler) {
        handler.before(this::validate, this::builder);
        handler.test(this::validate, this::builder);
        handler.after(this::validate, this::builder);
    }
}
