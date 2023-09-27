package net.neoforged.bus;

import org.apache.logging.log4j.LogManager;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

public final class EventBusEngine implements IEventBusEngine {
    private final EventAccessTransformer accessTransformer;
    final String EVENT_CLASS = "net.neoforged.bus.api.Event";

    public EventBusEngine() {
        LogManager.getLogger().debug(LogMarkers.EVENTBUS, "Loading EventBus transformers");
        this.accessTransformer = new EventAccessTransformer();
    }

    @Override
    public int processClass(final ClassNode classNode, final Type classType) {
        if (ModLauncherFactory.hasPendingWrapperClass(classType.getClassName())) {
            ModLauncherFactory.processWrapperClass(classType.getClassName(), classNode);
            LogManager.getLogger().debug(LogMarkers.EVENTBUS, "Built transformed event wrapper class {}", classType.getClassName());
            return ClassWriter.COMPUTE_FRAMES;
        }
        final int axXformFlags = accessTransformer.transform(classNode, classType) ? 0x100 : 0;
        return axXformFlags;
    }

    @Override
    public boolean handlesClass(final Type classType) {
        final String name = classType.getClassName();
        return !(name.startsWith("net.minecraft.") || name.indexOf('.') == -1);
    }

    @Override
    public boolean findASMEventDispatcher(final Type classType) {
        return ModLauncherFactory.hasPendingWrapperClass(classType.getClassName());
    }
}
