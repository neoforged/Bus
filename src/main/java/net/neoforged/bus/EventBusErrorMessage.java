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

import java.io.PrintWriter;
import java.io.StringWriter;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventListener;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.util.StringBuilderFormattable;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class EventBusErrorMessage implements Message, StringBuilderFormattable {
    //private final Event event;
    private final int index;
    private final EventListener[] listeners;
    private final Throwable throwable;

    public EventBusErrorMessage(final Event event, final int index, final EventListener[] listeners, final Throwable throwable) {
        //this.event = event;
        this.index = index;
        this.listeners = listeners;
        this.throwable = throwable;
    }

    @Override
    public String getFormattedMessage() {
        return "";
    }

    @Override
    public String getFormat() {
        return "";
    }

    @Override
    public Object[] getParameters() {
        return new Object[0];
    }

    @Override
    public Throwable getThrowable() {
        return null; // Cannot return the throwable here - it causes weird classloading issues inside log4j
    }

    @Override
    public void formatTo(final StringBuilder buffer) {
        buffer.append("Exception caught during firing event: ").append(throwable.getMessage()).append('\n').append("\tIndex: ").append(index).append('\n').append("\tListeners:\n");
        for (int x = 0; x < listeners.length; x++) {
            buffer.append("\t\t").append(x).append(": ").append(listeners[x]).append('\n');
        }
        final StringWriter sw = new StringWriter();
        throwable.printStackTrace(new PrintWriter(sw));
        buffer.append(sw.getBuffer());
    }
}
