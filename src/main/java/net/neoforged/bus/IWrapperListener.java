package net.neoforged.bus;

import net.neoforged.bus.api.IEventListener;

/**
 * Listener that wraps a listener to add a check.
 */
public interface IWrapperListener {
    IEventListener getWithoutCheck();
}
