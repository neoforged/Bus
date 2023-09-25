open module net.neoforged.bus.jmh {
    requires cpw.mods.modlauncher;
    requires cpw.mods.securejarhandler;

    requires org.junit.jupiter.api;
    requires org.objectweb.asm;
    requires org.objectweb.asm.tree;
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core;
    requires net.neoforged.bus;
    requires jopt.simple;
    requires jmh.core;
    requires cpw.mods.bootstraplauncher;

    requires static org.jetbrains.annotations;
    requires static net.neoforged.bus.testjars;

    exports net.neoforged.bus.benchmarks;

    provides cpw.mods.modlauncher.api.ITransformationService with net.neoforged.bus.benchmarks.MockTransformerService;

}