package org.dddjava.jig.infrastructure;

import java.nio.file.Path;

public interface Origin {
    Path[] extractClassPath();

    Path[] extractSourcePath();
}
