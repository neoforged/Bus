package net.minecraftforge.eventbus.api;

import net.minecraftforge.eventbus.BusBuilderImpl;

/**
 * Build a bus
 */
public interface BusBuilder {
    public static BusBuilder builder() {
        return new BusBuilderImpl();
    }

    BusBuilder setExceptionHandler(IEventExceptionHandler handler);
    BusBuilder startShutdown();
    BusBuilder checkTypesOnDispatch();
    BusBuilder markerType(Class<?> type);

    /* Use ModLauncher hooks when creating ASM handlers. */
    BusBuilder useModLauncher();

    IEventBus build();
}
