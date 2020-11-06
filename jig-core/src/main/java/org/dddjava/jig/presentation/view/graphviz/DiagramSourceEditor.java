package org.dddjava.jig.presentation.view.graphviz;

import org.dddjava.jig.domain.model.jigdocument.stationery.DiagramSources;

public interface DiagramSourceEditor<T> {

    DiagramSources edit(T model);
}
