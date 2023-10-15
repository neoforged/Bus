package net.neoforged.bus.api;

public interface IEventBusInvokeDispatcher {
    void invoke(EventListener listener, Event event);
}
