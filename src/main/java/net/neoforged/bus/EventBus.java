/*
 * Minecraft Forge
 * Copyright (c) 2016.
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */

package net.neoforged.bus;

import static net.neoforged.bus.LogMarkers.EVENTBUS;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.jodah.typetools.TypeResolver;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventListener;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.IEventClassChecker;
import net.neoforged.bus.api.IEventExceptionHandler;
import net.neoforged.bus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class EventBus implements IEventExceptionHandler, IEventBus {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final boolean checkTypesOnDispatchProperty = Boolean.parseBoolean(System.getProperty("eventbus.checkTypesOnDispatch", "false"));

    private final ConcurrentHashMap<Object, List<EventListener>> listeners = new ConcurrentHashMap<>();
    private final LockHelper<Class<?>, ListenerList> listenerLists = LockHelper.withIdentityHashMap();
    private final IEventExceptionHandler exceptionHandler;
    private volatile boolean shutdown = false;

    private final IEventClassChecker classChecker;
    private final boolean checkTypesOnDispatch;
    private final boolean allowPerPhasePost;

    @SuppressWarnings("unused")
    private EventBus() {
        this(new BusBuilderImpl());
    }

    private EventBus(final IEventExceptionHandler handler, boolean startShutdown, IEventClassChecker classChecker, boolean checkTypesOnDispatch, boolean allowPerPhasePost) {
        if (handler == null) exceptionHandler = this;
        else exceptionHandler = handler;
        this.shutdown = startShutdown;
        this.classChecker = classChecker;
        this.checkTypesOnDispatch = checkTypesOnDispatch || checkTypesOnDispatchProperty;
        this.allowPerPhasePost = allowPerPhasePost;
    }

    public EventBus(final BusBuilderImpl busBuilder) {
        this(busBuilder.exceptionHandler, busBuilder.startShutdown,
                busBuilder.classChecker, busBuilder.checkTypesOnDispatch, busBuilder.allowPerPhasePost);
    }

    @Override
    public void register(final Object target) {
        if (listeners.containsKey(target)) {
            return;
        }

        Class<?> type = target.getClass();
        if (type == Method.class) {
            var method = (Method) target;
            if (!Modifier.isStatic(method.getModifiers())) {
                throw new IllegalArgumentException("register() was called with a Method that is not static: " + method);
            }
            if (!method.isAnnotationPresent(SubscribeEvent.class)) {
                throw new IllegalArgumentException("register() was called with a Method that is not annotated with @SubscribeEvent: " + method);
            }

            registerListener(method, method);

            return;
        }

        boolean isStatic = type == Class.class;
        Class<?> clazz = isStatic ? (Class<?>) target : type;

        checkSupertypes(clazz, clazz);

        int foundMethods = 0;
        for (Method method : clazz.getDeclaredMethods()) {
            if (!method.isAnnotationPresent(SubscribeEvent.class)) {
                continue;
            }

            if (Modifier.isStatic(method.getModifiers()) == isStatic) {
                registerListener(target, method);
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
                    """.formatted(clazz));
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

    private void registerListener(final Object target, final Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length != 1) {
            throw new IllegalArgumentException(
                    "Method " + method + " has @SubscribeEvent annotation. " +
                            "It has " + parameterTypes.length + " arguments, " +
                            "but event handler methods require a single argument only.");
        }

        Class<?> eventType = parameterTypes[0];

        if (!Event.class.isAssignableFrom(eventType)) {
            throw new IllegalArgumentException(
                    "Method " + method + " has @SubscribeEvent annotation, " +
                            "but takes an argument that is not an Event subtype : " + eventType);
        }
        try {
            classChecker.check((Class<? extends Event>) eventType);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Method " + method + " has @SubscribeEvent annotation, " +
                            "but takes an argument that is not valid for this bus" + eventType,
                    e);
        }

        register(eventType, target, method);
    }

    @Nullable
    private <T extends Event> Predicate<T> passNotGenericFilter(boolean receiveCanceled) {
        // The cast is safe because the filter is removed if the event is not cancellable
        return receiveCanceled ? null : e -> !((ICancellableEvent) e).isCanceled();
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
        addListener(priority, passNotGenericFilter(receiveCanceled), eventType, consumer);
    }

    @SuppressWarnings("unchecked")
    private <T extends Event> Class<T> getEventClass(Consumer<T> consumer) {
        final Class<T> eventClass = (Class<T>) TypeResolver.resolveRawArgument(Consumer.class, consumer.getClass());
        if ((Class<?>) eventClass == TypeResolver.Unknown.class) {
            LOGGER.error(EVENTBUS, "Failed to resolve handler for \"{}\"", consumer.toString());
            throw new IllegalStateException("Failed to resolve consumer event type: " + consumer.toString());
        }
        return eventClass;
    }

    private <T extends Event> void addListener(final EventPriority priority, @Nullable Predicate<? super T> filter, final Consumer<T> consumer) {
        Class<T> eventClass = getEventClass(consumer);
        if (Objects.equals(eventClass, Event.class))
            LOGGER.warn(EVENTBUS, "Attempting to add a Lambda listener with computed generic type of Event. " +
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
        EventListener listener = filter == null ? new ConsumerEventHandler((Consumer<Event>) consumer) : new ConsumerEventHandler.WithPredicate((Consumer<Event>) consumer, (Predicate<Event>) filter);
        addToListeners(consumer, eventClass, listener, priority);
    }

    private void register(Class<?> eventType, Object target, Method method) {
        SubscribeEventListener listener = new SubscribeEventListener(target, method);
        addToListeners(target, eventType, listener, listener.getPriority());
    }

    private void addToListeners(final Object target, final Class<?> eventType, final EventListener listener, final EventPriority priority) {
        if (Modifier.isAbstract(eventType.getModifiers())) {
            throw new IllegalArgumentException(
                    "Cannot register listeners for abstract " + eventType +
                            ". Register a listener to one of its subclasses instead!");
        }
        getListenerList(eventType).register(priority, listener);
        List<EventListener> others = listeners.computeIfAbsent(target, k -> Collections.synchronizedList(new ArrayList<>()));
        others.add(listener);
    }

    private ListenerList getListenerList(Class<?> eventType) {
        ListenerList list = listenerLists.get(eventType);
        if (list != null) {
            return list;
        }

        if (Modifier.isAbstract(eventType.getSuperclass().getModifiers())) {
            validateAbstractChain(eventType.getSuperclass());

            return listenerLists.computeIfAbsent(eventType, e -> new ListenerList(e, allowPerPhasePost));
        } else {
            return listenerLists.computeIfAbsent(eventType, e -> new ListenerList(e, getListenerList(e.getSuperclass()), allowPerPhasePost));
        }
    }

    private static void validateAbstractChain(Class<?> eventType) {
        while (eventType != Event.class) {
            // Superclass must have the annotation
            if (!Modifier.isAbstract(eventType.getSuperclass().getModifiers())) {
                throw new IllegalArgumentException("Abstract event " + eventType +
                        " has a non-abstract superclass " + eventType.getSuperclass() +
                        ". The superclass must be made abstract.");
            }

            eventType = eventType.getSuperclass();
        }
    }

    @Override
    public void unregister(Object object) {
        List<EventListener> list = listeners.remove(object);
        if (list == null)
            return;
        for (ListenerList listenerList : listenerLists.getReadMap().values()) {
            for (EventListener listener : list) {
                listenerList.unregister(listener);
            }
        }
    }

    @Override
    public <T extends Event> T post(T event) {
        if (shutdown) {
            return event;
        }
        doPostChecks(event);

        return post(event, getListenerList(event.getClass()).getListeners());
    }

    @Override
    public <T extends Event> T post(EventPriority phase, T event) {
        if (!allowPerPhasePost) {
            throw new IllegalStateException("This bus does not allow calling phase-specific post.");
        }

        if (shutdown) {
            return event;
        }
        doPostChecks(event);

        return post(event, getListenerList(event.getClass()).getPhaseListeners(phase));
    }

    private void doPostChecks(Event event) {
        if (checkTypesOnDispatch) {
            try {
                classChecker.check(event.getClass());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                        "Cannot post event of type " + event.getClass().getSimpleName() + " to this bus", e);
            }
        }
    }

    private <T extends Event> T post(T event, EventListener[] listeners) {
        int index = 0;
        try {
            for (; index < listeners.length; index++) {
                listeners[index].invoke(event);
            }
        } catch (Throwable throwable) {
            exceptionHandler.handleException(this, event, listeners, index, throwable);
            throw throwable;
        }
        return event;
    }

    @Override
    public void handleException(IEventBus bus, Event event, EventListener[] listeners, int index, Throwable throwable) {
        LOGGER.error(EVENTBUS, () -> new EventBusErrorMessage(event, index, listeners, throwable));
    }

    @Override
    public void start() {
        this.shutdown = false;
    }
}
