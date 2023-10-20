package net.neoforged.bus.api;

import java.util.function.Consumer;

/**
 * EventBus API.
 *
 * Register for events and post events.
 *
 * Contains factory methods to construct an instance {@link #create()} and {@link #create(IEventExceptionHandler)}
 */
public interface IEventBus {
    /**
     * Register an instance object or a Class, and add listeners for all {@link SubscribeEvent} annotated methods
     * found there.
     *
     * Depending on what is passed as an argument, different listener creation behaviour is performed.
     *
     * <dl>
     *     <dt>Object Instance</dt>
     *     <dd>Scanned for <em>non-static</em> methods annotated with {@link SubscribeEvent} and creates listeners for
     *     each method found.</dd>
     *     <dt>Class Instance</dt>
     *     <dd>Scanned for <em>static</em> methods annotated with {@link SubscribeEvent} and creates listeners for
     *     each method found.</dd>
     * </dl>
     *
     * @param target Either a {@link Class} instance or an arbitrary object, for scanning and event listener creation
     */
    void register(Object target);

    /**
     * Add a consumer listener with default {@link EventPriority#NORMAL} and not recieving canceled events.
     *
     * @param consumer Callback to invoke when a matching event is received
     * @param <T> The {@link Event} subclass to listen for
     */
    <T extends Event> void addListener(Consumer<T> consumer);

    /**
     * Add a consumer listener not receiving canceled events.
     *
     * Use this method when one of the other methods fails to determine the concrete {@link Event} subclass that is
     * intended to be subscribed to.
     *
     * @param eventType The concrete {@link Event} subclass to subscribe to
     * @param consumer Callback to invoke when a matching event is received
     * @param <T> The {@link Event} subclass to listen for
     */
    <T extends Event> void addListener(Class<T> eventType, Consumer<T> consumer);

    /**
     * Add a consumer listener with the specified {@link EventPriority} and not receiving canceled events.
     *
     * @param priority {@link EventPriority} for this listener
     * @param consumer Callback to invoke when a matching event is received
     * @param <T> The {@link Event} subclass to listen for
     */
    <T extends Event> void addListener(EventPriority priority, Consumer<T> consumer);

    /**
     * Add a consumer listener with the specified {@link EventPriority} and not receiving canceled events.
     *
     * Use this method when one of the other methods fails to determine the concrete {@link Event} subclass that is
     * intended to be subscribed to.
     *
     * @param priority {@link EventPriority} for this listener
     * @param eventType The concrete {@link Event} subclass to subscribe to
     * @param consumer Callback to invoke when a matching event is received
     * @param <T> The {@link Event} subclass to listen for
     */
    <T extends Event> void addListener(EventPriority priority, Class<T> eventType, Consumer<T> consumer);

    /**
     * Add a consumer listener with the specified {@link EventPriority} and potentially canceled events.
     *
     * @param priority {@link EventPriority} for this listener
     * @param receiveCanceled Indicate if this listener should receive events that have been {@link Cancellable} canceled
     * @param consumer Callback to invoke when a matching event is received
     * @param <T> The {@link Event} subclass to listen for
     */
    <T extends Event> void addListener(EventPriority priority, boolean receiveCanceled, Consumer<T> consumer);

    /**
     * Add a consumer listener with the specified {@link EventPriority} and potentially canceled events.
     *
     * Use this method when one of the other methods fails to determine the concrete {@link Event} subclass that is
     * intended to be subscribed to.
     *
     * @param priority {@link EventPriority} for this listener
     * @param receiveCanceled Indicate if this listener should receive events that have been {@link Cancellable} canceled
     * @param eventType The concrete {@link Event} subclass to subscribe to
     * @param consumer Callback to invoke when a matching event is received
     * @param <T> The {@link Event} subclass to listen for
     */
    <T extends Event> void addListener(EventPriority priority, boolean receiveCanceled, Class<T> eventType, Consumer<T> consumer);

    /**
     * Add a consumer listener receiving potentially canceled events.
     *
     * @param receiveCanceled Indicate if this listener should receive events that have been {@link Cancellable} canceled
     * @param consumer Callback to invoke when a matching event is received
     * @param <T> The {@link Event} subclass to listen for
     */
    <T extends Event> void addListener(boolean receiveCanceled, Consumer<T> consumer);

    /**
     * Add a consumer listener receiving potentially canceled events.
     *
     * Use this method when one of the other methods fails to determine the concrete {@link Event} subclass that is
     * intended to be subscribed to.
     *
     * @param receiveCanceled Indicate if this listener should receive events that have been {@link Cancellable} canceled
     * @param eventType The concrete {@link Event} subclass to subscribe to
     * @param consumer Callback to invoke when a matching event is received
     * @param <T> The {@link Event} subclass to listen for
     */
    <T extends Event> void addListener(boolean receiveCanceled, Class<T> eventType, Consumer<T> consumer);

    /**
     * Add a consumer listener for a {@link GenericEvent} subclass, filtered to only be called for the specified
     * filter {@link Class}.
     *
     * @param genericClassFilter A {@link Class} which the {@link GenericEvent} should be filtered for
     * @param consumer Callback to invoke when a matching event is received
     * @param <T> The {@link GenericEvent} subclass to listen for
     * @param <F> The {@link Class} to filter the {@link GenericEvent} for
     * @deprecated Use non-generic events instead, or another system.
     */
    @Deprecated(forRemoval = true)
    <T extends Event & IGenericEvent<? extends F>, F> void addGenericListener(Class<F> genericClassFilter, Consumer<T> consumer);

    /**
     * Add a consumer listener with the specified {@link EventPriority} and not receiving canceled events,
     * for a {@link GenericEvent} subclass, filtered to only be called for the specified
     * filter {@link Class}.
     *
     * @param genericClassFilter A {@link Class} which the {@link GenericEvent} should be filtered for
     * @param priority {@link EventPriority} for this listener
     * @param consumer Callback to invoke when a matching event is received
     * @param <T> The {@link GenericEvent} subclass to listen for
     * @param <F> The {@link Class} to filter the {@link GenericEvent} for
     * @deprecated Use non-generic events instead, or another system.
     */
    @Deprecated(forRemoval = true)
    <T extends Event & IGenericEvent<? extends F>, F> void addGenericListener(Class<F> genericClassFilter, EventPriority priority, Consumer<T> consumer);

    /**
     * Add a consumer listener with the specified {@link EventPriority} and potentially canceled events,
     * for a {@link GenericEvent} subclass, filtered to only be called for the specified
     * filter {@link Class}.
     *
     * @param genericClassFilter A {@link Class} which the {@link GenericEvent} should be filtered for
     * @param priority {@link EventPriority} for this listener
     * @param receiveCanceled Indicate if this listener should receive events that have been {@link Cancellable} canceled
     * @param consumer Callback to invoke when a matching event is received
     * @param <T> The {@link GenericEvent} subclass to listen for
     * @param <F> The {@link Class} to filter the {@link GenericEvent} for
     * @deprecated Use non-generic events instead, or another system.
     */
    @Deprecated(forRemoval = true)
    <T extends Event & IGenericEvent<? extends F>, F> void addGenericListener(Class<F> genericClassFilter, EventPriority priority, boolean receiveCanceled, Consumer<T> consumer);

    /**
     * Add a consumer listener with the specified {@link EventPriority} and potentially canceled events,
     * for a {@link GenericEvent} subclass, filtered to only be called for the specified
     * filter {@link Class}.

     * Use this method when one of the other methods fails to determine the concrete {@link GenericEvent} subclass that is
     * intended to be subscribed to.
     *
     * @param genericClassFilter A {@link Class} which the {@link GenericEvent} should be filtered for
     * @param priority {@link EventPriority} for this listener
     * @param receiveCanceled Indicate if this listener should receive events that have been {@link Cancellable} canceled
     * @param eventType The concrete {@link GenericEvent} subclass to subscribe to
     * @param consumer Callback to invoke when a matching event is received
     * @param <T> The {@link GenericEvent} subclass to listen for
     * @param <F> The {@link Class} to filter the {@link GenericEvent} for
     * @deprecated Use non-generic events instead, or another system.
     */
    @Deprecated(forRemoval = true)
    <T extends Event & IGenericEvent<? extends F>, F> void addGenericListener(Class<F> genericClassFilter, EventPriority priority, boolean receiveCanceled, Class<T> eventType, Consumer<T> consumer);

    /**
     * Unregister the supplied listener from this EventBus.
     *
     * Removes all listeners from events.
     *
     * NOTE: Consumers can be stored in a variable if unregistration is required for the Consumer.
     *
     * @param object The object, {@link Class} or {@link Consumer} to unsubscribe.
     */
    void unregister(Object object);

    /**
     * Submit the event for dispatch to appropriate listeners
     * <p>
     * If this bus was not started yet, the event is returned without being dispatched.
     *
     * @param event The event to dispatch to listeners
     * @return the event that was passed in
     */
    <T extends Event> T post(T event);

    /**
     * Submit the event for dispatch to listeners registered with a specific {@link EventPriority}.
     * <p>
     * If this bus was not started yet, the event is returned without being dispatched.
     * <p>
     * Manually posting events phase-by-phase through this method is less performant
     * than dispatching to all phases through a {@link #post(Event)} call.
     * Prefer that method when per-phase dispatching is not needed.
     *
     * @param event The event to dispatch to listeners
     * @return the event that was passed in
     * @throws IllegalStateException if the bus does not allow per-phase post
     * @see BusBuilder#allowPerPhasePost()
     */
    <T extends Event> T post(EventPriority phase, T event);

    void start();
}
