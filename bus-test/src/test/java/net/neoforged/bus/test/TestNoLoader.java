/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.bus.test;

import net.neoforged.bus.test.general.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

public class TestNoLoader extends TestNoLoaderBase {

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
    public void testInvalidTypeNoDispatch() {
        doTest(new EventBusSubtypeFilterTest.InvalidNoDispatch() {});
    }

    @Test
    void eventHandlersCanFireEvents() {
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
    public void testNonPublicEventHandler() {
        doTest(new NonPublicEventHandler() {});
    }

    @Test
    public void testMethodEventHandler() {
        doTest(new MethodEventHandlerTest() {});
    }

    @Test
    public void testCommonSubscribeEventErrors() {
        doTest(new CommonSubscribeEventErrorsTest() {});
    }

    @Test
    public void testCancelledEvent() {
        doTest(new CancelledEventTest() {});
    }

    @Test
    public void testAbstractEventClasses() {
        doTest(new AbstractEventClassesTest() {});
    }
}
