package org.dddjava.jig.domain.basic;

import java.io.IOException;

public class FileWriteFailureException extends RuntimeException {
    public FileWriteFailureException(IOException e) {
        super(e);
    }
}
