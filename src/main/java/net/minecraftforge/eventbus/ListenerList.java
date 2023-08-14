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

package net.minecraftforge.eventbus;

import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

class ListenerList {
    private boolean rebuild = true;
    private AtomicReference<IEventListener[]> listeners = new AtomicReference<>();
    private final ArrayList<ArrayList<IEventListener>> priorities;
    private ListenerList parent;
    private List<ListenerList> children;
    private final Semaphore writeLock = new Semaphore(1, true);


    ListenerList()
    {
        int count = EventPriority.values().length;
        priorities = new ArrayList<>(count);

        for (int x = 0; x < count; x++)
        {
            priorities.add(new ArrayList<>());
        }
    }

    public void dispose()
    {
        writeLock.acquireUninterruptibly();
        priorities.forEach(ArrayList::clear);
        priorities.clear();
        writeLock.release();
        parent = null;
        listeners = null;
        if (children != null)
            children.clear();
    }

    ListenerList(ListenerList parent)
    {
        this();
        this.parent = parent;
        this.parent.addChild(this);
    }

    /**
     * Returns a ArrayList containing all listeners for this event,
     * and all parent events for the specified priority.
     *
     * The list is returned with the listeners for the children events first.
     *
     * @param priority The Priority to get
     * @return ArrayList containing listeners
     */
    public ArrayList<IEventListener> getListeners(EventPriority priority)
    {
        writeLock.acquireUninterruptibly();
        ArrayList<IEventListener> ret = new ArrayList<>(priorities.get(priority.ordinal()));
        writeLock.release();
        if (parent != null)
        {
            ret.addAll(parent.getListeners(priority));
        }
        return ret;
    }

    /**
     * Returns a full list of all listeners for all priority levels.
     * Including all parent listeners.
     *
     * List is returned in proper priority order.
     *
     * Automatically rebuilds the internal Array cache if its information is out of date.
     *
     * @return Array containing listeners
     */
    public IEventListener[] getListeners()
    {
        if (shouldRebuild()) buildCache();
        return listeners.get();
    }

    protected boolean shouldRebuild()
    {
        return rebuild;// || (parent != null && parent.shouldRebuild());
    }

    protected void forceRebuild()
    {
        this.rebuild = true;
        if (this.children != null) {
            synchronized (this.children) {
                for (ListenerList child : this.children)
                    child.forceRebuild();
            }
        }
    }

    private void addChild(ListenerList child)
    {
        if (this.children == null)
            this.children = Collections.synchronizedList(new ArrayList<>(2));
        this.children.add(child);
    }

    /**
     * Rebuild the local Array of listeners, returns early if there is no work to do.
     */
    private void buildCache()
    {
        if (parent != null && parent.shouldRebuild())
        {
            parent.buildCache();
        }
        ArrayList<IEventListener> ret = new ArrayList<>();
        Arrays.stream(EventPriority.values()).forEach(value -> {
            List<IEventListener> listeners = getListeners(value);
            if (listeners.size() > 0) {
                ret.add(value); //Add the priority to notify the event of it's current phase.
                ret.addAll(listeners);
            }
        });
        this.listeners.set(ret.toArray(new IEventListener[0]));
        rebuild = false;
    }

    public void register(EventPriority priority, IEventListener listener)
    {
        writeLock.acquireUninterruptibly();
        priorities.get(priority.ordinal()).add(listener);
        writeLock.release();
        this.forceRebuild();
    }

    public void unregister(IEventListener listener)
    {
        writeLock.acquireUninterruptibly();
        priorities.stream().filter(list -> list.remove(listener)).forEach(list -> this.forceRebuild());
        writeLock.release();
    }
}
