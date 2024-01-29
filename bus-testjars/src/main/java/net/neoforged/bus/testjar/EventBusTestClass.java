/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.bus.testjar;

import net.neoforged.bus.api.SubscribeEvent;

public class EventBusTestClass {
    public boolean HIT1= false;
    public boolean HIT2= false;
    @SubscribeEvent
    public void eventMethod(DummyEvent evt) {
        HIT1 = true;
    }

    @SubscribeEvent
    public void eventMethod2(DummyEvent.GoodEvent evt) {
        HIT2 = true;
    }

    @SubscribeEvent
    public void evtMethod3(DummyEvent.CancellableEvent evt) {

    }

    @SubscribeEvent
    public void badEventMethod(DummyEvent.BadEvent evt) {
        throw new RuntimeException("BARF");
    }
}
