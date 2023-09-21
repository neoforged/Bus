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
    default BusBuilder markerType(Class<?> markerInterface) {
        if (!markerInterface.isInterface()) throw new IllegalArgumentException("Cannot specify a class marker type");
        return classChecker(eventType -> {
            if (!markerInterface.isAssignableFrom(eventType)) {
                throw new IllegalArgumentException("This bus only accepts subclasses of " + markerInterface + ", which " + eventType + " is not.");
            }
        });
    }
    BusBuilder classChecker(IEventClassChecker checker);

    /* Use ModLauncher hooks when creating ASM handlers. */
    BusBuilder useModLauncher();

    IEventBus build();
}
