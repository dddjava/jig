package org.dddjava.jig.presentation.view.graphvizj;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import org.dddjava.jig.presentation.view.JigView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;

public abstract class GraphvizjView<T> implements JigView<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceAngleToImageView.class);

    public void render(T model, OutputStream outputStream) throws IOException {
        String graphText = graphText(model);

        LOGGER.debug(graphText);

        Graphviz.fromString(graphText)
                .render(Format.PNG)
                .toOutputStream(outputStream);
    }

    protected abstract String graphText(T model);
}
