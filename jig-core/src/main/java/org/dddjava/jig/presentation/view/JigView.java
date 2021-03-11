package org.dddjava.jig.presentation.view;

import java.io.IOException;

public interface JigView {

    void render(Object model, JigDocumentWriter jigDocumentWriter) throws IOException;
}
