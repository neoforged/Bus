open module net.neoforged.bus {
    requires cpw.mods.modlauncher;

    requires org.objectweb.asm;
    requires org.objectweb.asm.tree;
    requires org.apache.logging.log4j;
    requires static org.jetbrains.annotations;
    requires net.jodah.typetools;

    exports net.minecraftforge.eventbus;
    exports net.minecraftforge.eventbus.api;
}