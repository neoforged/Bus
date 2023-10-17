package net.neoforged.bus;

import net.neoforged.bus.api.EventListener;

/**
 * Listener that wraps a listener to add a check.
 */
public interface IWrapperListener {
    EventListener getWithoutCheck();
}
