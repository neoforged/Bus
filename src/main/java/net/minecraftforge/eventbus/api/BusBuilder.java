package net.minecraftforge.eventbus.api;

import java.util.function.Function;
import java.util.function.Predicate;

import net.minecraftforge.eventbus.BusBuilderImpl;

/**
 * Build a bus
 */
public interface BusBuilder {
    public static BusBuilder builder() {
        return new BusBuilderImpl();
    }

    /* true by default */
    BusBuilder setTrackPhases(boolean trackPhases);
    BusBuilder setExceptionHandler(IEventExceptionHandler handler);
    BusBuilder startShutdown();
    BusBuilder checkTypesOnDispatch();
    default BusBuilder markerType(Class<?> type) {
        if (!type.isInterface()) throw new IllegalArgumentException("Cannot specify a class marker type");
        return eventClassFilter(
                type::isAssignableFrom,
                eventClass -> "This bus only accepts subclasses of " + type + ", which " + eventClass + " is not."
        );
    }
    BusBuilder eventClassFilter(Predicate<Class<? extends Event>> filter, Function<Class<? extends Event>, String> errorMessageSupplier);

    /* Use ModLauncher hooks when creating ASM handlers. */
    BusBuilder useModLauncher();

    IEventBus build();
}
