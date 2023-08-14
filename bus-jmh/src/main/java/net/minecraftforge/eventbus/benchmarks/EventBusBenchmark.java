package net.minecraftforge.eventbus.benchmarks;

import net.minecraftforge.eventbus.benchmarks.compiled.BenchmarkArmsLength;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

@State(Scope.Benchmark)
public class EventBusBenchmark {
    @Benchmark
    public int testDynamic() {
        BenchmarkArmsLength.postDynamic(BenchmarkArmsLength.instance);
        return 0;
    }

    @Benchmark
    public int testLambda() {
        BenchmarkArmsLength.postLambda(BenchmarkArmsLength.instance);
        return 0;
    }

    @Benchmark
    public int testStatic() {
        BenchmarkArmsLength.postStatic(BenchmarkArmsLength.instance);
        return 0;
    }

    @Benchmark
    public int testCombined() {
        BenchmarkArmsLength.postCombined(BenchmarkArmsLength.instance);
        return 0;
    }
}
