import org.jspecify.annotations.NullMarked;

@NullMarked
module org.dddjava.jig.core {
    exports org.dddjava.jig;
    exports org.dddjava.jig.domain.model.sources;
    exports org.dddjava.jig.infrastructure.configuration;
    exports org.dddjava.jig.domain.model.documents.documentformat;
    exports org.dddjava.jig.domain.model.sources.filesystem;

    requires com.github.benmanes.caffeine;
    requires com.github.javaparser.core;
    requires java.logging;
    requires micrometer.core;
    requires micrometer.registry.prometheus;
    requires org.apache.commons.compress;
    requires org.apache.poi.ooxml;
    requires org.apache.poi.poi;
    requires org.mybatis;
    requires org.objectweb.asm;
    requires org.slf4j;
    requires thymeleaf;
    requires org.jspecify;
}