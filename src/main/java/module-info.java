open module net.neoforged.bus {
    requires org.objectweb.asm;
    requires org.apache.logging.log4j;
    requires static org.jetbrains.annotations;
    requires net.jodah.typetools;

    exports net.neoforged.bus;
    exports net.neoforged.bus.api;
}