package org.dddjava.jig.infrastructure.javaparser;

import java.nio.file.Path;

public class JavaParserFailException extends RuntimeException {
    public JavaParserFailException(Path path, Exception e) {
        super(path.toString(), e);
    }
}
