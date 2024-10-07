package org.dddjava.jig.application;

import org.dddjava.jig.domain.model.sources.jigfactory.TextSourceModel;

public interface JigSourceRepository {

    void registerTextSourceModel(TextSourceModel textSourceModel);
}
