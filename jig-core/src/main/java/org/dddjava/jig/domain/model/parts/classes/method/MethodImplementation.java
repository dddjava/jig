package org.dddjava.jig.domain.model.parts.classes.method;

import java.util.Optional;

/**
 * 実装されたメソッド
 *
 * テキストソース由来の情報
 */
public class MethodImplementation {
    MethodIdentifier methodIdentifier;
    MethodComment methodComment;

    public MethodImplementation(MethodIdentifier methodIdentifier, MethodComment methodComment) {
        this.methodIdentifier = methodIdentifier;
        this.methodComment = methodComment;
    }

    public MethodImplementation(MethodIdentifier methodIdentifier) {
        this(methodIdentifier, null);
    }

    public Optional<MethodComment> comment() {
        return Optional.ofNullable(methodComment);
    }
}
