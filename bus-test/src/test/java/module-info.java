open module net.neoforged.bus.test {
    requires org.junit.jupiter.api;
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core;
    requires net.neoforged.bus;
    requires jopt.simple;

    requires static org.jetbrains.annotations;

    requires static net.neoforged.bus.testjars;

    exports net.neoforged.bus.test;
}