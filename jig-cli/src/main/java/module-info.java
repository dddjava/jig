import org.jspecify.annotations.NullMarked;

@NullMarked
module org.dddjava.jig.cli {
    requires org.dddjava.jig.core;
    requires org.jspecify;
    requires org.slf4j;
    requires spring.beans;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
}