package net.minecraftforge.eventbus;

import java.util.function.Function;
import java.util.function.Predicate;

import net.minecraftforge.eventbus.api.BusBuilder;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.IEventExceptionHandler;

/**
 * BusBuilder Implementation, public for BusBuilder.builder() only, don't use this directly.
 */
public final class BusBuilderImpl implements BusBuilder {
    IEventExceptionHandler exceptionHandler;
    boolean trackPhases = true;
    boolean startShutdown = false;
    boolean checkTypesOnDispatch = false;
    Predicate<Class<? extends Event>> eventFilter = eventClass -> true;
    Function<Class<? extends Event>, String> errorMessageSupplier = eventClass -> "Unreachable";
    boolean modLauncher = false;

    @Override
    public BusBuilder setTrackPhases(boolean trackPhases) {
        this.trackPhases = trackPhases;
        return this;
    }

    @Override
    public BusBuilder setExceptionHandler(IEventExceptionHandler handler) {
        this.exceptionHandler =  handler;
        return this;
    }

    @Override
    public BusBuilder startShutdown() {
        this.startShutdown = true;
        return this;
    }

    @Override
    public BusBuilder checkTypesOnDispatch() {
        this.checkTypesOnDispatch = true;
        return this;
    }

    @Override
    public BusBuilder eventClassFilter(Predicate<Class<? extends Event>> filter, Function<Class<? extends Event>, String> errorMessageSupplier) {
        this.eventFilter = filter;
        this.errorMessageSupplier = errorMessageSupplier;
        return this;
    }

    @Override
    public BusBuilder useModLauncher() {
        this.modLauncher = true;
        return this;
    }

    @Override
    public IEventBus build() {
        return new EventBus(this);
    }
}
