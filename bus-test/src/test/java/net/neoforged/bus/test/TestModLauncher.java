package net.neoforged.bus.test;

import org.junit.jupiter.api.Test;

import net.neoforged.bus.test.general.AbstractEventListenerTest;
import net.neoforged.bus.test.general.DeadlockingEventTest;
import net.neoforged.bus.test.general.EventBusSubtypeFilterTest;
import net.neoforged.bus.test.general.EventClassCheckTest;
import net.neoforged.bus.test.general.EventFiringEventTest;
import net.neoforged.bus.test.general.EventHandlerExceptionTest;
import net.neoforged.bus.test.general.GenericListenerTests;
import net.neoforged.bus.test.general.LambdaHandlerTest;
import net.neoforged.bus.test.general.NonPublicEventHandler;
import net.neoforged.bus.test.general.ParallelEventTest;
import net.neoforged.bus.test.general.ParentHandlersGetInvokedTest;
import net.neoforged.bus.test.general.ParentHandlersGetInvokedTestDummy;
import net.neoforged.bus.test.general.ThreadedListenerExceptionTest;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.RepeatedTest;

public class TestModLauncher extends TestModLauncherBase {
    @Test
    public void eventHandlersCanSubscribeToAbstractEvents() {
        doTest(new AbstractEventListenerTest() {});
    }

    @RepeatedTest(10)
    public void testMultipleThreadsMultipleBus() {
        doTest(new ParallelEventTest.Multiple() {});
    }

    @RepeatedTest(100)
    public void testMultipleThreadsOneBus() {
        doTest(new ParallelEventTest.Single() {});
    }

    @Test
    public void testEventHandlerException() {
        doTest(new EventHandlerExceptionTest() {});
    }

    @Test
    public void testValidType() {
        doTest(new EventBusSubtypeFilterTest.Valid() {});
    }

    @Test
    public void testInvalidType() {
        doTest(new EventBusSubtypeFilterTest.Invalid() {});
    }

    @Test
    public void eventHandlersCanFireEvents() {
        doTest(new EventFiringEventTest() {});
    }

    @Test
    public void eventClassChecks() {
        doTest(new EventClassCheckTest() {});
    }

    @Test
    public void lambdaBasic() {
        doTest(new LambdaHandlerTest.Basic() {});
    }

    @Test
    public void lambdaSubClass() {
        doTest(new LambdaHandlerTest.SubClassEvent() {});
    }

    @Test
    public void lambdaGenerics() {
        doTest(new LambdaHandlerTest.Generics() {});
    }

    @Disabled
    @RepeatedTest(500)
    public void deadlockTest() {
        doTest(new DeadlockingEventTest() {});
    }

    @Test
    public void parentHandlerGetsInvoked() {
        doTest(new ParentHandlersGetInvokedTest() {});
    }

    @Test
    public void parentHandlerGetsInvokedDummy() {
        doTest(new ParentHandlersGetInvokedTestDummy() {});
    }

    @RepeatedTest(100)
    public void testThreadedEventFiring() {
        doTest(new ThreadedListenerExceptionTest() {});
    }

    @Test
    public void testGenericListener() {
        doTest(new GenericListenerTests.Basic() {});
    }

    @Test
    public void testGenericListenerRegisteredIncorrectly() {
        doTest(new GenericListenerTests.IncorrectRegistration() {});
    }

    @Test
    public void testGenericListenerWildcard() {
        doTest(new GenericListenerTests.Wildcard() {});
    }

    @Test
    public void testNonPublicEventHandler() {
        doTest(new NonPublicEventHandler(true) {});
    }

}
