/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.bus;

import net.neoforged.bus.api.*;

/**
 * BusBuilder Implementation, public for BusBuilder.builder() only, don't use this directly.
 */
public final class BusBuilderImpl implements BusBuilder {
    IEventExceptionHandler exceptionHandler;
    boolean startShutdown = false;
    boolean checkTypesOnDispatch = false;
    IEventClassChecker classChecker = eventClass -> {};
    boolean allowPerPhasePost = false;

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
    public BusBuilder allowPerPhasePost() {
        this.allowPerPhasePost = true;
        return this;
    }

    @Override
    public IEventBus build() {
        return new EventBus(this);
    }
}
