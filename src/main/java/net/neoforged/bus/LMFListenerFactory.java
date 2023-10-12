package net.neoforged.bus;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.IEventListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class LMFListenerFactory implements IEventListenerFactory {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final MethodHandles.Lookup IMPL_LOOKUP;

    private static final LockHelper<Method, MethodHandle> eventListenerFactories = LockHelper.withHashMap();

    static {
        try {
            var hackfield = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            hackfield.setAccessible(true);
            IMPL_LOOKUP = (MethodHandles.Lookup) hackfield.get(null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to access IMPL_LOOKUP", e);
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

    @Override
    public IEventListener create(Method callback, Object target) {
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
