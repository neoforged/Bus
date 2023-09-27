package net.neoforged.bus.test;

import net.neoforged.bus.api.BusBuilder;

public class TestNoLoaderBase {
    private BusBuilder builder() {
        return BusBuilder.builder();
    }

    protected void doTest(ITestHandler handler) {
        handler.before(this::builder);
        handler.test(this::builder);
        handler.after(this::builder);
    }
}
