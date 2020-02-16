package org.dddjava.jig.domain.model.jigdocument;

import org.dddjava.jig.domain.model.jigmodel.analyzed.Warning;

public interface JigLogger {
    void warn(Warning warning);
}
