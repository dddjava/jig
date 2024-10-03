package org.dddjava.jig.application;

import java.io.IOException;

public interface JigView {

    void render(Object model, JigDocumentWriter jigDocumentWriter) throws IOException;
}
