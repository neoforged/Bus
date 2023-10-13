package net.neoforged.bus;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.IEventListener;
import net.neoforged.bus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Manages generation of {@link IEventListener} instances from a {@link SubscribeEvent} method,
 * using {@link LambdaMetafactory}.
 */
class EventListenerFactory {
    private static final MethodHandles.Lookup IMPL_LOOKUP;

    private static final LockHelper<Method, MethodHandle> eventListenerFactories = LockHelper.withHashMap();

    static {
        try {
            var hackfield = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            hackfield.setAccessible(true);
            IMPL_LOOKUP = (MethodHandles.Lookup) hackfield.get(null);
        } catch (Exception e) {
            throw new RuntimeException("""
        Failed to access IMPL_LOOKUP.
        Maybe you need to add --add-opens="java.base/java.lang.invoke=net.neoforged.bus" to your JVM arguments.
        """, e);
        }
    }

    private static final MethodType LISTENER_INVOKE = MethodType.methodType(void.class, Event.class);

    private static MethodHandle getEventListenerFactory(Method m) {
        return eventListenerFactories.computeIfAbsent(m, callback -> {
            try {
                var callbackClass = callback.getDeclaringClass();
                var lookup = IMPL_LOOKUP.in(callbackClass);

                if (Modifier.isStatic(callback.getModifiers())) {
                    return LambdaMetafactory.metafactory(
                            lookup,
                            "invoke",
                            MethodType.methodType(IEventListener.class),
                            LISTENER_INVOKE,
                            lookup.unreflect(callback),
                            MethodType.methodType(void.class, callback.getParameterTypes()[0])
                    ).getTarget();
                } else {
                    return LambdaMetafactory.metafactory(
                            lookup,
                            "invoke",
                            MethodType.methodType(IEventListener.class, callbackClass),
                            LISTENER_INVOKE,
                            lookup.unreflect(callback),
                            MethodType.methodType(void.class, callback.getParameterTypes()[0])
                    ).getTarget();
                }
            } catch (Throwable e) {
                throw new RuntimeException("Failed to create IEventListener factory", e);
            }
        });
    }

    public static IEventListener create(Method callback, Object target) {
        try {
            var factory = getEventListenerFactory(callback);

            if (Modifier.isStatic(callback.getModifiers())) {
                return (IEventListener) factory.invoke();
            } else {
                return (IEventListener) factory.invoke(target);
            }
        } catch (Throwable e) {
            throw new RuntimeException("Failed to create IEventListener", e);
        }
    }
}
