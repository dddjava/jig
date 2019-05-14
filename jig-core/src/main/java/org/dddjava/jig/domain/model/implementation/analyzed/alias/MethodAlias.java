package org.dddjava.jig.domain.model.implementation.analyzed.alias;

import org.dddjava.jig.domain.model.implementation.analyzed.declaration.method.MethodIdentifier;

/**
 * メソッド別名
 */
public class MethodAlias {
    MethodIdentifier methodIdentifier;
    Alias alias;

    public MethodAlias(MethodIdentifier methodIdentifier, Alias alias) {
        this.methodIdentifier = methodIdentifier;
        this.alias = alias;
    }

    public MethodIdentifier methodIdentifier() {
        return methodIdentifier;
    }

    public Alias japaneseName() {
        return alias;
    }
}
