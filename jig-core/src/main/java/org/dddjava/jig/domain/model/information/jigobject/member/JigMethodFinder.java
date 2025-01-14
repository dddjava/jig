package org.dddjava.jig.domain.model.information.jigobject.member;

import org.dddjava.jig.domain.model.data.classes.method.MethodIdentifier;

import java.util.Optional;

public interface JigMethodFinder {

    Optional<JigMethod> find(MethodIdentifier methodIdentifier);
}
