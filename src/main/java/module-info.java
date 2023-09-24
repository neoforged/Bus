open module net.neoforged.bus {
    uses net.neoforged.bus.IEventBusEngine;
    requires cpw.mods.modlauncher;

    requires org.objectweb.asm;
    requires org.objectweb.asm.tree;
    requires org.apache.logging.log4j;
    requires static org.jetbrains.annotations;
    requires net.jodah.typetools;

    exports net.neoforged.bus;
    exports net.neoforged.bus.api;
    provides cpw.mods.modlauncher.serviceapi.ILaunchPluginService with net.neoforged.bus.service.ModLauncherService;
    provides net.neoforged.bus.IEventBusEngine with net.neoforged.bus.EventBusEngine;
}