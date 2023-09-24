package net.neoforged.bus.api;

@FunctionalInterface
public interface IEventClassChecker {
    /**
     * If the event class should be accepted for the bus, does nothing.
     * If it should not be accepted, throws an {@link IllegalArgumentException}.
     *
     * @throws IllegalArgumentException If the event class is not valid.
     */
    void check(Class<? extends Event> eventClass) throws IllegalArgumentException;
}
