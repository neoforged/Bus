/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.bus.testjar;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventListener;

public class TestListener extends EventListener {
    private Object instance;

    TestListener(Object instance) {
        this.instance = instance;
    }

    @Override
    public void invoke(final Event event) {
        instance.equals(event);
    }
}
