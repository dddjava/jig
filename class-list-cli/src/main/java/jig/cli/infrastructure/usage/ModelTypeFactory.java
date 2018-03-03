package jig.cli.infrastructure.usage;

import jig.domain.model.usage.ModelType;

import java.nio.file.Path;

public interface ModelTypeFactory {

    boolean isTargetClass(Path path);

    ModelType toModelType(Class<?> clz);
}
