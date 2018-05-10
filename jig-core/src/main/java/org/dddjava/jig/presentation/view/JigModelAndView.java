package org.dddjava.jig.presentation.view;

import java.io.IOException;
import java.io.OutputStream;

public class JigModelAndView<T> {

    T model;
    JigView<T> view;

    public JigModelAndView(T model, JigView<T> view) {
        this.model = model;
        this.view = view;
    }

    public void render(OutputStream outputStream) throws IOException {
        view.render(model, outputStream);
    }
}
