package net.neoforged.bus.testjar;

import net.neoforged.bus.api.Event;

public class DummyEvent extends Event {
    public static class GoodEvent extends DummyEvent {}
    public static class BadEvent extends DummyEvent {}
    public static class CancellableEvent extends DummyEvent implements net.neoforged.bus.api.CancellableEvent {}
    @HasResult
    public static class ResultEvent extends DummyEvent {}
}
