package org.dddjava.jig.infrastructure;

import java.nio.file.Path;

public interface Layout {
    Path[] extractClassPath();

    Path[] extractSourcePath();
}
