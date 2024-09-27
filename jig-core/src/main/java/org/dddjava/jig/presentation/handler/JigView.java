package org.dddjava.jig.presentation.handler;

import java.io.IOException;

public interface JigView {

    void render(Object model, JigDocumentWriter jigDocumentWriter) throws IOException;
}
