package org.dddjava.jig.domain.model.implementation.analyzed.alias;

import org.dddjava.jig.domain.model.implementation.analyzed.declaration.method.MethodIdentifier;

/**
 * メソッド和名
 */
public class MethodJapaneseName {
    MethodIdentifier methodIdentifier;
    JapaneseName japaneseName;

    public MethodJapaneseName(MethodIdentifier methodIdentifier, JapaneseName japaneseName) {
        this.methodIdentifier = methodIdentifier;
        this.japaneseName = japaneseName;
    }

    public MethodIdentifier methodIdentifier() {
        return methodIdentifier;
    }

    public JapaneseName japaneseName() {
        return japaneseName;
    }
}
