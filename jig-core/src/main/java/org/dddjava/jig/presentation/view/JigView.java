package org.dddjava.jig.presentation.view;

import java.io.IOException;

public interface JigView<T> {

    void render(T model, JigDocumentLocation jigDocumentLocation) throws IOException;
}
