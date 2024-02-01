/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.bus.benchmarks.compiled;


import net.neoforged.bus.api.SubscribeEvent;

public class SubscriberDynamic
{
    @SubscribeEvent
    public void onCancellableEvent(CancellableEvent event)
    {

    }

    @SubscribeEvent
    public void onSimpleEvent(EventWithData event)
    {

    }
}
