package net.neoforged.bus.benchmarks.compiled;

import net.neoforged.bus.api.IEventBus;

public class SubscriberLambda
{

    public static void register(IEventBus bus)
    {
        bus.addListener(SubscriberLambda::onCancelableEvent);
        bus.addListener(SubscriberLambda::onResultEvent);
        bus.addListener(SubscriberLambda::onSimpleEvent);
    }

    public static void onCancelableEvent(CancelableEvent event)
    {

    }

    public static void onResultEvent(ResultEvent event)
    {

    }

    public static void onSimpleEvent(EventWithData event)
    {

    }
}
