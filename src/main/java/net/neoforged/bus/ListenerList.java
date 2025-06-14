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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;
import net.neoforged.bus.api.EventListener;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.ICancellableEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public class ListenerList {
    private static final EventPriority[] PRIORITIES = EventPriority.values();

    private boolean rebuild = true;
    private final AtomicReference<EventListener[]> listeners = new AtomicReference<>();
    private final AtomicReference<EventListener[][]> perPhaseListeners = new AtomicReference<>();
    private final ArrayList<ArrayList<EventListener>> priorities;
    @Nullable
    private final ListenerList parent;
    private List<ListenerList> children;
    private final Semaphore writeLock = new Semaphore(1, true);
    private final boolean canUnwrapListeners;
    private final boolean buildPerPhaseList;

    ListenerList(Class<?> eventClass, boolean buildPerPhaseList) {
        this(eventClass, null, buildPerPhaseList);
    }

    ListenerList(Class<?> eventClass, @Nullable ListenerList parent, boolean buildPerPhaseList) {
        int count = PRIORITIES.length;
        priorities = new ArrayList<>(count);

        for (int x = 0; x < count; x++) {
            priorities.add(new ArrayList<>());
        }

        // Unwrap if the event is not cancellable
        canUnwrapListeners = !ICancellableEvent.class.isAssignableFrom(eventClass);
        this.buildPerPhaseList = buildPerPhaseList;

        this.parent = parent;
        if (parent != null) {
            parent.addChild(this);
        }
    }

    /**
     * Returns a ArrayList containing all listeners for this event,
     * and all parent events for the specified priority.
     * <p>
     * The list is returned with the listeners for the children events first.
     *
     * @param priority The Priority to get
     * @return ArrayList containing listeners
     */
    private ArrayList<EventListener> getListeners(EventPriority priority) {
        writeLock.acquireUninterruptibly();
        ArrayList<EventListener> ret = new ArrayList<>(priorities.get(priority.ordinal()));
        writeLock.release();
        if (parent != null) {
            ret.addAll(parent.getListeners(priority));
        }
        return ret;
    }

    /**
     * {@return a full list of all listeners for all priority levels}
     * Including all parent listeners.
     * <p>
     * List is returned in proper priority order.
     * <p>
     * Automatically rebuilds the internal Array cache if its information is out of date.
     */
    public EventListener[] getListeners() {
        if (shouldRebuild()) buildCache();
        return listeners.get();
    }

    public EventListener[] getPhaseListeners(EventPriority phase) {
        if (!buildPerPhaseList) {
            throw new IllegalStateException("buildPerPhaseList is false!");
        }

        if (shouldRebuild()) buildCache();
        return perPhaseListeners.get()[phase.ordinal()];
    }

    protected boolean shouldRebuild() {
        return rebuild;
    }

    protected void forceRebuild() {
        this.rebuild = true;
        if (this.children != null) {
            synchronized (this.children) {
                for (ListenerList child : this.children)
                    child.forceRebuild();
            }
        }
    }

    private void addChild(ListenerList child) {
        if (this.children == null)
            this.children = Collections.synchronizedList(new ArrayList<>(2));
        this.children.add(child);
    }

    /**
     * Rebuild the local Array of listeners, returns early if there is no work to do.
     */
    private void buildCache() {
        if (parent != null && parent.shouldRebuild()) {
            parent.buildCache();
        }
        ArrayList<EventListener> ret = new ArrayList<>();
        EventListener[][] perPhaseListeners = buildPerPhaseList ? new EventListener[PRIORITIES.length][] : null;

        for (EventPriority phase : PRIORITIES) {
            var phaseListeners = getListeners(phase);
            unwrapListeners(phaseListeners);
            ret.addAll(phaseListeners);

            if (perPhaseListeners != null) {
                perPhaseListeners[phase.ordinal()] = phaseListeners.toArray(EventListener[]::new);
            }
        }

        this.listeners.set(ret.toArray(new EventListener[0]));
        this.perPhaseListeners.set(perPhaseListeners);

        rebuild = false;
    }

    private void unwrapListeners(List<EventListener> ret) {
        if (canUnwrapListeners) {
            for (int i = 0; i < ret.size(); ++i) {
                if (ret.get(i) instanceof IWrapperListener wrapper) {
                    ret.set(i, wrapper.getWithoutCheck());
                }
            }
        }
    }

    public void register(EventPriority priority, EventListener listener) {
        writeLock.acquireUninterruptibly();
        priorities.get(priority.ordinal()).add(listener);
        writeLock.release();
        this.forceRebuild();
    }

    public void unregister(EventListener listener) {
        writeLock.acquireUninterruptibly();
        priorities.stream().filter(list -> list.remove(listener)).forEach(list -> this.forceRebuild());
        writeLock.release();
    }
}
