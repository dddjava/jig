package org.dddjava.jig.presentation.view.graphvizj;

import org.dddjava.jig.domain.model.jigdocument.DiagramSource;

public interface DiagramSourceEditor<T> {

    DiagramSource edit(T model);
}
