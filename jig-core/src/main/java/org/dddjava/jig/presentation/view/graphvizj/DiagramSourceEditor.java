package org.dddjava.jig.presentation.view.graphvizj;

import org.dddjava.jig.domain.model.jigdocument.stationery.DiagramSources;

public interface DiagramSourceEditor<T> {

    DiagramSources edit(T model);
}
