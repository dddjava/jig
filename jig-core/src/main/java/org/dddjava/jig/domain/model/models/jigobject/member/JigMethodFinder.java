package org.dddjava.jig.domain.model.models.jigobject.member;

import org.dddjava.jig.domain.model.parts.classes.method.MethodIdentifier;

import java.util.Optional;

public interface JigMethodFinder {

    Optional<JigMethod> find(MethodIdentifier methodIdentifier);
}
