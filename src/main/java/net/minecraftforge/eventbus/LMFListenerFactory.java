package net.minecraftforge.eventbus;

import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventListener;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class LMFListenerFactory implements IEventListenerFactory {
    /**
     * We would prefer to use privateLookupIn(), but that does not set the MODULE bit,
     * which LMF requires for class definition. So we have to use the IMPL_LOOKUP.
     */
    private static final MethodHandles.Lookup IMPL_LOOKUP;

    static {
        try {
            var hackfield = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            hackfield.setAccessible(true);
            IMPL_LOOKUP = (MethodHandles.Lookup) hackfield.get(null);
        } catch (Exception e) {
            // TODO: if this fails I suppose we could fall back to a slower methodhandle invoke
            throw new RuntimeException("Failed to access IMPL_LOOKUP...");
        }
    }

    private static final MethodType LISTENER_INVOKE = MethodType.methodType(void.class, Event.class);

    @Override
    public IEventListener create(Method callback, Object target) {
        try {
            var callbackClass = callback.getDeclaringClass();
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
