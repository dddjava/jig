package org.dddjava.jig.presentation.view;

import java.io.IOException;

public class JigModelAndView<T> {

    T model;
    JigView<T> view;

    public JigModelAndView(T model, JigView<T> view) {
        this.model = model;
        this.view = view;
    }

    public void render(JigDocumentLocation jigDocumentLocation) throws IOException {
        view.render(model, jigDocumentLocation);
    }
}
