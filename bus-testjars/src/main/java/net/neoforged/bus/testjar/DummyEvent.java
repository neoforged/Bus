package net.neoforged.bus.testjar;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public class DummyEvent extends Event {
    public static class GoodEvent extends DummyEvent {}
    public static class BadEvent extends DummyEvent {}
    public static class CancellableEvent extends DummyEvent implements ICancellableEvent {}
    @HasResult
    public static class ResultEvent extends DummyEvent {}
}
