package net.neoforged.bus;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.IEventListener;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class MethodHandleFactory implements IEventListenerFactory {
    @Override
    public IEventListener create(Method callback, Object target) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException {
        var callbackClass = callback.getDeclaringClass();
        getClass().getModule().addReads(callbackClass.getModule());
        var lookup = MethodHandles.privateLookupIn(callbackClass, MethodHandles.lookup());
        callback.setAccessible(true);
        var methodHandle = lookup.unreflect(callback);

        if (Modifier.isStatic(callback.getModifiers())) {
            var updatedMH = methodHandle.asType(MethodType.methodType(void.class, Event.class));
            return event -> {
                try {
                    updatedMH.invokeExact(event);
                } catch (Throwable throwable) {
                    throw new RuntimeException(throwable);
                }
            };
        } else {
            var updatedMH = methodHandle.asType(MethodType.methodType(void.class, Object.class, Event.class));
            return event -> {
                try {
                    updatedMH.invokeExact(target, event);
                } catch (Throwable throwable) {
                    throw new RuntimeException(throwable);
                }
            };
        }
    }
}
