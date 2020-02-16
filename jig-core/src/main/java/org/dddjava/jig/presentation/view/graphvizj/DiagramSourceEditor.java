package org.dddjava.jig.presentation.view.graphvizj;

import org.dddjava.jig.domain.model.jigdocument.DotText;

public interface DiagramSourceEditor<T> {

    DotText edit(T model);

    default DiagramSource editDiagramSourceFrom(T model) {
        return new DiagramSource(edit(model));
    }
}
