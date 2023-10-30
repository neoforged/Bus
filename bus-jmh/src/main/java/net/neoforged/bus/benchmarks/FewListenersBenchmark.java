/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.bus.benchmarks;

import net.neoforged.bus.benchmarks.compiled.BenchmarkArmsLength;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

@State(Scope.Benchmark)
public class FewListenersBenchmark {
    // No Runtime Patching
    @Benchmark
    public int testDynamic() {
        BenchmarkArmsLength.postDynamic(BenchmarkArmsLength.NoLoader);
        return 0;
    }

    @Benchmark
    public int testLambda() {
        BenchmarkArmsLength.postLambda(BenchmarkArmsLength.NoLoader);
        return 0;
    }

    @Benchmark
    public int testStatic() {
        BenchmarkArmsLength.postStatic(BenchmarkArmsLength.NoLoader);
        return 0;
    }

    @Benchmark
    public int testCombined() {
        BenchmarkArmsLength.postCombined(BenchmarkArmsLength.NoLoader);
        return 0;
    }
}
