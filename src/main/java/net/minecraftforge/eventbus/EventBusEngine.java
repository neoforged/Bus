package net.minecraftforge.eventbus;

import org.apache.logging.log4j.LogManager;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

public final class EventBusEngine implements IEventBusEngine {
    private final EventSubclassTransformer eventTransformer;
    final String EVENT_CLASS = "net.minecraftforge.eventbus.api.Event";

    public EventBusEngine() {
        LogManager.getLogger().debug(LogMarkers.EVENTBUS, "Loading EventBus transformers");
        this.eventTransformer = new EventSubclassTransformer();
    }

    @Override
    public int processClass(final ClassNode classNode, final Type classType) {
        return eventTransformer.transform(classNode, classType).isPresent() ? ClassWriter.COMPUTE_FRAMES : 0x0;
    }

    @Override
    public boolean handlesClass(final Type classType) {
        final String name = classType.getClassName();
        return !(name.startsWith("net.minecraft.") || name.indexOf('.') == -1);
    }

    @Override
    public boolean findASMEventDispatcher(final Type classType) {
        return false;
    }
}
