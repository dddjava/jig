package jig.cli.infrastructure.usage;

import java.nio.file.Path;

public interface ModelTypeFactory {

    boolean isTargetClass(Path path);
}
