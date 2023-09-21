package net.minecraftforge.eventbus;

import net.minecraftforge.eventbus.api.BusBuilder;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.IEventClassChecker;
import net.minecraftforge.eventbus.api.IEventExceptionHandler;

/**
 * BusBuilder Implementation, public for BusBuilder.builder() only, don't use this directly.
 */
public final class BusBuilderImpl implements BusBuilder {
    IEventExceptionHandler exceptionHandler;
    boolean startShutdown = false;
    boolean checkTypesOnDispatch = false;
    IEventClassChecker classChecker = eventClass -> {};
    boolean modLauncher = false;

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
    public BusBuilder classChecker(IEventClassChecker checker) {
        this.classChecker = checker;
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
