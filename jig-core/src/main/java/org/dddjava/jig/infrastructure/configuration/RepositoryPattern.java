package org.dddjava.jig.infrastructure.configuration;

import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCode;

public class RepositoryPattern {
    String pattern;

    public RepositoryPattern(String pattern) {
        this.pattern = pattern;
    }

    public RepositoryPattern() {
        this(".+Repository");
    }

    public boolean matches(TypeByteCode typeByteCode) {
        return typeByteCode.typeIdentifier().fullQualifiedName().matches(pattern);
    }

}
