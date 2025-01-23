package org.dddjava.jig.domain.model.data.classes.method;

import java.util.Optional;

public interface JigMethodFinder {

    Optional<JigMethod> find(MethodIdentifier methodIdentifier);
}
