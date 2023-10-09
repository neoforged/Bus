package net.neoforged.bus.test;

import cpw.mods.bootstraplauncher.BootstrapLauncher;
import cpw.mods.modlauncher.TransformingClassLoader;
import cpw.mods.modlauncher.api.ServiceRunner;
import net.neoforged.bus.api.BusBuilder;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import org.junit.jupiter.api.BeforeAll;

public class TestModLauncherBase {
    private static final String CLASS_NAME = "test.modlauncher.class";
    private static final String METHOD_NAME = "test.modlauncher.method";
    private static final String RUNNING_TEST = "test.modlauncher.running";

    private static TransformingClassLoader classLoader;

    BusBuilder builder() {
        return BusBuilder.builder().useModLauncher();
    }

    @BeforeAll
    public static void setupTransformingClassLoader() {
        String paths;
        try {
            paths = MockTransformerService.getTestJarsPath() + "," + MockTransformerService.getBasePath();
        } catch (Exception e) {
            if (e instanceof RuntimeException re)
                throw re;
            throw new RuntimeException(e);
        }
        System.setProperty("test.harness.game", paths);
        System.setProperty("test.harness.callable", TestCallback.class.getName());
        BootstrapLauncher.main("--version", "1.0", "--launchTarget", "testharness");

        if (!(Thread.currentThread().getContextClassLoader() instanceof TransformingClassLoader transformingClassLoader))
            throw new RuntimeException("Failed to setup transforming class loader.");

        classLoader = transformingClassLoader;
    }

    protected void doTest(ITestHandler handler) {
        if (System.getProperty(RUNNING_TEST) != null) {
            handler.before(this::builder);
            handler.test(this::builder);
            handler.after(this::builder);
        } else {
            // Otherwise: reload the class in the transforming class loader.
            // WARNING: terrible hack!
            var method = handler.getClass().getEnclosingMethod();
            var methodName = method.getName();
            var className = method.getDeclaringClass().getName();

            try {
                var inst = Class.forName(className, true, classLoader).getConstructor().newInstance();

                getClass().getModule().addReads(inst.getClass().getModule());
                var handle = MethodHandles.lookup().findVirtual(inst.getClass(), methodName, MethodType.methodType(void.class));

                System.setProperty(RUNNING_TEST, "true");

                handle.invoke(inst);
            } catch (Throwable ex) {
                throw new RuntimeException("Failed to run test with transforming class loader", ex);
            } finally {
                System.clearProperty(RUNNING_TEST);
            }
        }
    }

    public static class TestCallback {
        public static ServiceRunner supplier() {
            // Return a NO-OP ServiceRunner to continue JUnit testing.
            return ServiceRunner.NOOP;
        }
    }
}
