package org.dddjava.jig.presentation.view.handler;

import java.io.IOException;

public class FileWriteFailureException extends RuntimeException {
    public FileWriteFailureException(IOException e) {
        super(e);
    }
}
