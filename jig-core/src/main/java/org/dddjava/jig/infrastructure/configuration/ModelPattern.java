package org.dddjava.jig.infrastructure.configuration;

import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCode;

public class ModelPattern {
    String pattern;

    public ModelPattern(String pattern) {
        this.pattern = pattern;
    }

    public ModelPattern() {
        this(".+\\.domain\\.model\\..+");
    }

    public boolean matches(TypeByteCode typeByteCode) {
        return typeByteCode.typeIdentifier().fullQualifiedName().matches(pattern);
    }
}
