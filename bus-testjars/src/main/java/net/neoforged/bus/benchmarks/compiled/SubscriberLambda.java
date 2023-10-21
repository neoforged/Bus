/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.bus.benchmarks.compiled;

import net.neoforged.bus.api.IEventBus;

public class SubscriberLambda
{

    public static void register(IEventBus bus)
    {
        bus.addListener(SubscriberLambda::onCancellableEvent);
        bus.addListener(SubscriberLambda::onResultEvent);
        bus.addListener(SubscriberLambda::onSimpleEvent);
    }

    public static void onCancellableEvent(CancellableEvent event)
    {

    }

    public static void onResultEvent(ResultEvent event)
    {

    }

    public static void onSimpleEvent(EventWithData event)
    {

    }
}
