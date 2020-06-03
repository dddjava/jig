package org.dddjava.jig.presentation.view.graphvizj;

import org.dddjava.jig.domain.model.jigdocumenter.DiagramSources;

public interface DiagramSourceEditor<T> {

    DiagramSources edit(T model);
}
