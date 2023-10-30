/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.bus;

import net.neoforged.bus.api.EventListener;

/**
 * Listener that wraps a listener to add a check.
 */
public interface IWrapperListener {
    EventListener getWithoutCheck();
}
