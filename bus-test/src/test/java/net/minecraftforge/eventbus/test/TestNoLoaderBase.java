package net.minecraftforge.eventbus.test;

import net.minecraftforge.eventbus.api.BusBuilder;
import static org.junit.jupiter.api.Assertions.*;

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
