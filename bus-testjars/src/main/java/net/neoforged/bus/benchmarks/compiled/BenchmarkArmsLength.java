package net.neoforged.bus.benchmarks.compiled;

import java.util.function.Consumer;

import net.neoforged.bus.api.BusBuilder;
import net.neoforged.bus.api.FactoryType;
import net.neoforged.bus.api.IEventBus;

public class BenchmarkArmsLength
{
    public record Bus(
        IEventBus staticSubscriberBus,
        IEventBus dynamicSubscriberBus,
        IEventBus lambdaSubscriberBus,
        IEventBus combinedSubscriberBus
    ) {
        public Bus register() {
            staticSubscriberBus.register(SubscriberStatic.class);
            combinedSubscriberBus.register(SubscriberStatic.class);
            dynamicSubscriberBus.register(new SubscriberDynamic());
            combinedSubscriberBus.register(new SubscriberDynamic());
            SubscriberLambda.register(lambdaSubscriberBus);
            SubscriberLambda.register(combinedSubscriberBus);
            return this;
        }
    };

    public static Runnable supplier() {
        return () -> {
            ModLauncher = new Bus(
                BusBuilder.builder().useModLauncher().build(),
                BusBuilder.builder().useModLauncher().build(),
                BusBuilder.builder().useModLauncher().build(),
                BusBuilder.builder().useModLauncher().build()
            ).register();
            ClassLoader = new Bus(
                BusBuilder.builder().build(),
                BusBuilder.builder().build(),
                BusBuilder.builder().build(),
                BusBuilder.builder().build()
            ).register();
            MethodHandles = new Bus(
                    BusBuilder.builder().factoryType(FactoryType.METHOD_HANDLES).build(),
                    BusBuilder.builder().factoryType(FactoryType.METHOD_HANDLES).build(),
                    BusBuilder.builder().factoryType(FactoryType.METHOD_HANDLES).build(),
                    BusBuilder.builder().factoryType(FactoryType.METHOD_HANDLES).build()
            ).register();
            LMF = new Bus(
                    BusBuilder.builder().factoryType(FactoryType.LAMBDA_META_FACTORY).build(),
                    BusBuilder.builder().factoryType(FactoryType.LAMBDA_META_FACTORY).build(),
                    BusBuilder.builder().factoryType(FactoryType.LAMBDA_META_FACTORY).build(),
                    BusBuilder.builder().factoryType(FactoryType.LAMBDA_META_FACTORY).build()
            ).register();
        };
    }

    public static Bus ModLauncher;
    public static Bus ClassLoader;
    public static Bus MethodHandles;
    public static Bus LMF;
    public static Bus NoLoader = new Bus(
        BusBuilder.builder().build(),
        BusBuilder.builder().build(),
        BusBuilder.builder().build(),
        BusBuilder.builder().build()
    ).register();

    public static final Consumer<Object> postStatic = BenchmarkArmsLength::postStatic;
    public static final Consumer<Object> postDynamic = BenchmarkArmsLength::postDynamic;
    public static final Consumer<Object> postLambda = BenchmarkArmsLength::postLambda;
    public static final Consumer<Object> postCombined = BenchmarkArmsLength::postCombined;

    public static void postStatic(Object bus)
    {
        postAll(((Bus)bus).staticSubscriberBus);
    }

    public static void postDynamic(Object bus)
    {
        postAll(((Bus)bus).dynamicSubscriberBus);
    }

    public static void postLambda(Object bus)
    {
        postAll(((Bus)bus).lambdaSubscriberBus);
    }

    public static void postCombined(Object bus)
    {
        postAll(((Bus)bus).combinedSubscriberBus);
    }

    private static void postAll(IEventBus bus)
    {
        bus.post(new CancellableEvent());
        bus.post(new ResultEvent());
        bus.post(new EventWithData("Foo", 5, true)); //Some example data
    }
}
