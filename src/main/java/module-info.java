/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

open module net.neoforged.bus {
    requires org.objectweb.asm;
    requires org.apache.logging.log4j;
    requires static org.jetbrains.annotations;
    requires net.jodah.typetools;
    requires jdk.unsupported; // required for typetools

    exports net.neoforged.bus;
    exports net.neoforged.bus.api;
}
