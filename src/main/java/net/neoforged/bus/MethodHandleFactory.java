package net.neoforged.bus;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.IEventListener;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class MethodHandleFactory implements IEventListenerFactory {
    private static final LockHelper<Method, MethodHandle> methodHandles = LockHelper.withHashMap();

    private static MethodHandle getMethodHandle(Method m) {
        return methodHandles.computeIfAbsent(m, callback -> {
            try {
                var callbackClass = callback.getDeclaringClass();
                MethodHandleFactory.class.getModule().addReads(callbackClass.getModule());
                var lookup = MethodHandles.privateLookupIn(callbackClass, MethodHandles.lookup());
                callback.setAccessible(true);
                var methodHandle = lookup.unreflect(callback);

                if (Modifier.isStatic(callback.getModifiers())) {
                    return methodHandle.asType(MethodType.methodType(void.class, Event.class));
                } else {
                    return methodHandle.asType(MethodType.methodType(void.class, Object.class, Event.class));
                }
            } catch (Throwable e) {
                throw new RuntimeException("Failed to create method handle", e);
            }
        });
    }

    @Override
    public IEventListener create(Method callback, Object target) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException {
        var methodHandle = getMethodHandle(callback);

        if (Modifier.isStatic(callback.getModifiers())) {
            return event -> {
                try {
                    methodHandle.invokeExact(event);
                } catch (Throwable throwable) {
                    throw new RuntimeException(throwable);
                }
            };
        } else {
            return event -> {
                try {
                    methodHandle.invokeExact(target, event);
                } catch (Throwable throwable) {
                    throw new RuntimeException(throwable);
                }
            };
        }
    }
}
