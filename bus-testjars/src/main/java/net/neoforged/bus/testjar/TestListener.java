package net.neoforged.bus.testjar;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.IEventListener;

public class TestListener implements IEventListener {
    private Object instance;

    TestListener(Object instance) {
        this.instance = instance;
    }

    @Override
    public void invoke(final Event event) {
        instance.equals(event);
    }
}
