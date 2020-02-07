package org.dddjava.jig.presentation.view.graphvizj;

import org.dddjava.jig.domain.model.jigdocument.DotText;

public interface DotTextEditor<T> {

    DotText edit(T model);

    default DotTexts dotTexts(T model) {
        return new DotTexts(edit(model));
    }
}
