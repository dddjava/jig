package org.dddjava.jig.presentation.view;

import java.io.IOException;
import java.io.OutputStream;

public interface JigView<T> {

    void render(T model, OutputStream outputStream) throws IOException;
}
