package net.neoforged.bus.testjar;

import net.neoforged.bus.api.Cancelable;
import net.neoforged.bus.api.Event;

public class DummyEvent extends Event {
    public static class GoodEvent extends DummyEvent {}
    public static class BadEvent extends DummyEvent {}
    @Cancelable
    public static class CancellableEvent extends DummyEvent {}
    @HasResult
    public static class ResultEvent extends DummyEvent {}
}
