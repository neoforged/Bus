package net.neoforged.bus.api;

public interface IEventBusInvokeDispatcher {
    void invoke(IEventListener listener, Event event);
}
