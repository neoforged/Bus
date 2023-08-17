open module net.neoforged.bus.test {
    requires cpw.mods.modlauncher;
    requires cpw.mods.securejarhandler;
    requires cpw.mods.bootstraplauncher;

    requires org.junit.jupiter.api;
    requires org.objectweb.asm;
    requires org.objectweb.asm.tree;
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core;
    requires net.neoforged.bus;
    requires jopt.simple;

    requires static org.jetbrains.annotations;

    requires static net.neoforged.bus.testjars;

    exports net.minecraftforge.eventbus.test;

    provides cpw.mods.modlauncher.api.ITransformationService with net.minecraftforge.eventbus.test.MockTransformerService;
}