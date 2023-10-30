/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

open module net.neoforged.bus.jmh {
    requires org.junit.jupiter.api;
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core;
    requires net.neoforged.bus;
    requires jopt.simple;
    requires jmh.core;

    requires static org.jetbrains.annotations;
    requires static net.neoforged.bus.testjars;

    exports net.neoforged.bus.benchmarks;
}