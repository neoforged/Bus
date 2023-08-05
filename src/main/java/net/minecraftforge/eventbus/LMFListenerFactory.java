package net.minecraftforge.eventbus;

import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventListener;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class LMFListenerFactory implements IEventListenerFactory {
    private static final MethodHandles.Lookup IMPL_LOOKUP;

    static {
        try {
            var hackfield = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            hackfield.setAccessible(true);
            IMPL_LOOKUP = (MethodHandles.Lookup) hackfield.get(null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to access IMPL_LOOKUP...");
        }
    }

    private static final MethodType LISTENER_INVOKE = MethodType.methodType(void.class, Event.class);

    @Override
    public IEventListener create(Method callback, Object target) {
        try {
            var callbackClass = callback.getDeclaringClass();

            // We need to read the other module to perform java.lang.invoke operations with it
            getClass().getModule().addReads(callbackClass.getModule());

            // Sadly this doesn't grant the MODULE bit and thus doesn't allow defining new classes in the module.
            //var lookup = MethodHandles.privateLookupIn(callbackClass, MethodHandles.lookup());
            var lookup = IMPL_LOOKUP.in(callbackClass);

            if (Modifier.isStatic(callback.getModifiers())) {
                return (IEventListener) LambdaMetafactory.metafactory(
                        lookup,
                        "invoke",
                        MethodType.methodType(IEventListener.class),
                        LISTENER_INVOKE,
                        lookup.unreflect(callback),
                        MethodType.methodType(void.class, callback.getParameterTypes()[0])
                ).getTarget().invoke();
            } else {
                return (IEventListener) LambdaMetafactory.metafactory(
                        lookup,
                        "invoke",
                        MethodType.methodType(IEventListener.class, callbackClass),
                        LISTENER_INVOKE,
                        lookup.unreflect(callback),
                        MethodType.methodType(void.class, callback.getParameterTypes()[0])
                ).getTarget().invoke(target);
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
