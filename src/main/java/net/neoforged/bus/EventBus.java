/*
 * Minecraft Forge
 * Copyright (c) 2016.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package net.neoforged.bus;

import net.jodah.typetools.TypeResolver;
import net.neoforged.bus.api.*;
import net.neoforged.bus.api.EventListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static net.neoforged.bus.LogMarkers.EVENTBUS;

public class EventBus implements IEventExceptionHandler, IEventBus {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final boolean checkTypesOnDispatchProperty = Boolean.parseBoolean(System.getProperty("eventbus.checkTypesOnDispatch", "false"));

    private ConcurrentHashMap<Object, List<EventListener>> listeners = new ConcurrentHashMap<>();
    private final LockHelper<Class<?>, ListenerList> listenerLists = LockHelper.withIdentityHashMap();
    private final IEventExceptionHandler exceptionHandler;
    private volatile boolean shutdown = false;

    private final IEventClassChecker classChecker;
    private final boolean checkTypesOnDispatch;

    @SuppressWarnings("unused")
    private EventBus() {
        this(new BusBuilderImpl());
    }

    private EventBus(final IEventExceptionHandler handler, boolean startShutdown, IEventClassChecker classChecker, boolean checkTypesOnDispatch) {
        if (handler == null) exceptionHandler = this;
        else exceptionHandler = handler;
        this.shutdown = startShutdown;
        this.classChecker = classChecker;
        this.checkTypesOnDispatch = checkTypesOnDispatch || checkTypesOnDispatchProperty;
    }

    public EventBus(final BusBuilderImpl busBuilder) {
        this(busBuilder.exceptionHandler, busBuilder.startShutdown,
             busBuilder.classChecker, busBuilder.checkTypesOnDispatch);
    }

    @Override
    public void register(final Object target)
    {
        if (listeners.containsKey(target))
        {
            return;
        }

        boolean isStatic = target.getClass() == Class.class;
        Class<?> clazz = isStatic ? (Class<?>) target : target.getClass();

        checkSupertypes(clazz, clazz);

        int foundMethods = 0;
        for (Method method : clazz.getDeclaredMethods()) {
            if (!method.isAnnotationPresent(SubscribeEvent.class)) {
                continue;
            }

            if (Modifier.isStatic(method.getModifiers()) == isStatic) {
                registerListener(target, method, method);
            } else {
                if (isStatic) {
                    throw new IllegalArgumentException("""
                            Expected @SubscribeEvent method %s to be static
                            because register() was called with a class type.
                            Either make the method static, or call register() with an instance of %s.
                            """.formatted(method, clazz));
                } else {
                    throw new IllegalArgumentException("""
                            Expected @SubscribeEvent method %s to NOT be static
                            because register() was called with an instance type.
                            Either make the method non-static, or call register(%s.class).
                            """.formatted(method, clazz.getSimpleName()));
                }
            }

            ++foundMethods;
        }

        if (foundMethods == 0) {
            throw new IllegalArgumentException("""
                    %s has no @SubscribeEvent methods, but register was called anyway.
                    The event bus only recognizes listener methods that have the @SubscribeEvent annotation.
                    """.formatted(clazz)
            );
        }
    }

    private static void checkSupertypes(Class<?> registeredType, Class<?> type) {
        if (type == null || type == Object.class) {
            return;
        }

        if (type != registeredType) {
            for (var method : type.getDeclaredMethods()) {
                if (method.isAnnotationPresent(SubscribeEvent.class)) {
                    throw new IllegalArgumentException("""
                            Attempting to register a listener object of type %s,
                            however its supertype %s has a @SubscribeEvent method: %s.
                            This is not allowed! Only the listener object can have @SubscribeEvent methods.
                            """.formatted(registeredType, type, method));
                }
            }
        }

        checkSupertypes(registeredType, type.getSuperclass());
        Stream.of(type.getInterfaces())
                .forEach(itf -> checkSupertypes(registeredType, itf));
    }

    private void registerListener(final Object target, final Method method, final Method real) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length != 1)
        {
            throw new IllegalArgumentException(
                    "Method " + method + " has @SubscribeEvent annotation. " +
                    "It has " + parameterTypes.length + " arguments, " +
                    "but event handler methods require a single argument only."
            );
        }

        Class<?> eventType = parameterTypes[0];

        if (!Event.class.isAssignableFrom(eventType))
        {
            throw new IllegalArgumentException(
                    "Method " + method + " has @SubscribeEvent annotation, " +
                            "but takes an argument that is not an Event subtype : " + eventType);
        }
        try {
            classChecker.check((Class<? extends Event>) eventType);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Method " + method + " has @SubscribeEvent annotation, " +
                            "but takes an argument that is not valid for this bus" + eventType, e);
        }

        register(eventType, target, real);
    }

    @Nullable
    private <T extends Event> Predicate<T> passNotGenericFilter(boolean receiveCanceled) {
        // The cast is safe because the filter is removed if the event is not cancellable
        return receiveCanceled ? null : e -> !((ICancellableEvent) e).isCanceled();
    }

    private <T extends IGenericEvent<? extends F>, F> Predicate<T> passGenericFilter(Class<F> type, boolean receiveCanceled) {
        return receiveCanceled ? e -> e.getGenericType() == type : e -> e.getGenericType() == type && !(e instanceof ICancellableEvent cancellable && cancellable.isCanceled());
    }

    private void checkNotGeneric(final Consumer<? extends Event> consumer) {
        checkNotGeneric(getEventClass(consumer));
    }

    private void checkNotGeneric(final Class<? extends Event> eventType) {
        if (IGenericEvent.class.isAssignableFrom(eventType)) {
            throw new IllegalArgumentException("Cannot register a generic event listener with addListener, use addGenericListener");
        }
    }

    @Override
    public <T extends Event> void addListener(final Consumer<T> consumer) {
        addListener(EventPriority.NORMAL, consumer);
    }

    @Override
    public <T extends Event> void addListener(final EventPriority priority, final Consumer<T> consumer) {
        addListener(priority, false, consumer);
    }

    @Override
    public <T extends Event> void addListener(final EventPriority priority, final boolean receiveCanceled, final Consumer<T> consumer) {
        checkNotGeneric(consumer);
        addListener(priority, passNotGenericFilter(receiveCanceled), consumer);
    }

    @Override
    public <T extends Event> void addListener(EventPriority priority, Class<T> eventType, Consumer<T> consumer) {
        addListener(priority, false, eventType, consumer);
    }

    @Override
    public <T extends Event> void addListener(boolean receiveCanceled, Consumer<T> consumer) {
        addListener(EventPriority.NORMAL, receiveCanceled, consumer);
    }

    @Override
    public <T extends Event> void addListener(boolean receiveCanceled, Class<T> eventType, Consumer<T> consumer) {
        addListener(EventPriority.NORMAL, receiveCanceled, eventType, consumer);
    }

    @Override
    public <T extends Event> void addListener(Class<T> eventType, Consumer<T> consumer) {
        addListener(EventPriority.NORMAL, false, eventType, consumer);
    }

    @Override
    public <T extends Event> void addListener(final EventPriority priority, final boolean receiveCanceled, final Class<T> eventType, final Consumer<T> consumer) {
        checkNotGeneric(eventType);
        addListener(priority, passNotGenericFilter(receiveCanceled), eventType, consumer);
    }

    @Override
    public <T extends Event & IGenericEvent<? extends F>, F> void addGenericListener(final Class<F> genericClassFilter, final Consumer<T> consumer) {
        addGenericListener(genericClassFilter, EventPriority.NORMAL, consumer);
    }

    @Override
    public <T extends Event & IGenericEvent<? extends F>, F> void addGenericListener(final Class<F> genericClassFilter, final EventPriority priority, final Consumer<T> consumer) {
        addGenericListener(genericClassFilter, priority, false, consumer);
    }

    @Override
    public <T extends Event & IGenericEvent<? extends F>, F> void addGenericListener(final Class<F> genericClassFilter, final EventPriority priority, final boolean receiveCanceled, final Consumer<T> consumer) {
        addListener(priority, passGenericFilter(genericClassFilter, receiveCanceled), consumer);
    }

    @Override
    public <T extends Event & IGenericEvent<? extends F>, F> void addGenericListener(final Class<F> genericClassFilter, final EventPriority priority, final boolean receiveCanceled, final Class<T> eventType, final Consumer<T> consumer) {
        addListener(priority, passGenericFilter(genericClassFilter, receiveCanceled), eventType, consumer);
    }

    @SuppressWarnings("unchecked")
    private <T extends Event> Class<T> getEventClass(Consumer<T> consumer) {
        final Class<T> eventClass = (Class<T>) TypeResolver.resolveRawArgument(Consumer.class, consumer.getClass());
        if ((Class<?>)eventClass == TypeResolver.Unknown.class) {
            LOGGER.error(EVENTBUS, "Failed to resolve handler for \"{}\"", consumer.toString());
            throw new IllegalStateException("Failed to resolve consumer event type: " + consumer.toString());
        }
        return eventClass;
    }

    private <T extends Event> void addListener(final EventPriority priority, @Nullable Predicate<? super T> filter, final Consumer<T> consumer) {
        Class<T> eventClass = getEventClass(consumer);
        if (Objects.equals(eventClass, Event.class))
            LOGGER.warn(EVENTBUS,"Attempting to add a Lambda listener with computed generic type of Event. " +
                    "Are you sure this is what you meant? NOTE : there are complex lambda forms where " +
                    "the generic type information is erased and cannot be recovered at runtime.");
        addListener(priority, filter, eventClass, consumer);
    }

    private <T extends Event> void addListener(final EventPriority priority, @Nullable Predicate<? super T> filter, final Class<T> eventClass, final Consumer<T> consumer) {
        try {
            classChecker.check(eventClass);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Listener for event " + eventClass + " takes an argument that is not valid for this bus", e);
        }
        EventListener listener = filter == null ?
                new ConsumerEventHandler((Consumer<Event>) consumer) :
                new ConsumerEventHandler.WithPredicate((Consumer<Event>) consumer, (Predicate<Event>) filter);
        addToListeners(consumer, eventClass, listener, priority);
    }

    private void register(Class<?> eventType, Object target, Method method)
    {
        SubscribeEventListener listener = new SubscribeEventListener(target, method, IGenericEvent.class.isAssignableFrom(eventType));
        addToListeners(target, eventType, listener, listener.getPriority());
    }

    private void addToListeners(final Object target, final Class<?> eventType, final EventListener listener, final EventPriority priority) {
        getListenerList(eventType).register(priority, listener);
        List<EventListener> others = listeners.computeIfAbsent(target, k -> Collections.synchronizedList(new ArrayList<>()));
        others.add(listener);
    }

    private ListenerList getListenerList(Class<?> eventType) {
        ListenerList list = listenerLists.get(eventType);
        if (list != null) {
            return list;
        }

        if (eventType == Event.class) {
            return listenerLists.computeIfAbsent(eventType, ListenerList::new);
        } else {
            return listenerLists.computeIfAbsent(eventType, e -> new ListenerList(e, getListenerList(e.getSuperclass())));
        }
    }

    @Override
    public void unregister(Object object)
    {
        List<EventListener> list = listeners.remove(object);
        if(list == null)
            return;
        for (ListenerList listenerList : listenerLists.getReadMap().values()) {
            for (EventListener listener : list) {
                listenerList.unregister(listener);
            }
        }
    }

    @Override
    public <T extends Event> T post(T event) {
        return post(event, (EventListener::invoke));
    }

    @Override
    public < T extends Event> T post(T event, IEventBusInvokeDispatcher wrapper)
    {
        if (shutdown)
        {
            throw new IllegalStateException("Attempted to post event of type " +
                    event.getClass().getSimpleName() + " on a bus that was not started yet!");
        }
        if (checkTypesOnDispatch)
        {
            try {
                classChecker.check(event.getClass());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                        "Cannot post event of type " + event.getClass().getSimpleName() + " to this bus", e);
            }
        }

        EventListener[] listeners = getListenerList(event.getClass()).getListeners();
        int index = 0;
        try
        {
            for (; index < listeners.length; index++)
            {
                wrapper.invoke(listeners[index], event);
            }
        }
        catch (Throwable throwable)
        {
            exceptionHandler.handleException(this, event, listeners, index, throwable);
            throw throwable;
        }
        return event;
    }

    @Override
    public void handleException(IEventBus bus, Event event, EventListener[] listeners, int index, Throwable throwable)
    {
        LOGGER.error(EVENTBUS, ()->new EventBusErrorMessage(event, index, listeners, throwable));
    }

    @Override
    public void start() {
        this.shutdown = false;
    }
}
