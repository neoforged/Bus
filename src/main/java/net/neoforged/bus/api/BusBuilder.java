package net.neoforged.bus.api;

import net.neoforged.bus.BusBuilderImpl;

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

    IEventBus build();
}
