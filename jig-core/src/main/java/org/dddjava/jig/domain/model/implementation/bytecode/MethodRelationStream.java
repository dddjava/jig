package org.dddjava.jig.domain.model.implementation.bytecode;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.declaration.method.MethodIdentifier;

import java.util.stream.Stream;

/**
 * メソッドの使用しているメソッド一覧ストリーム
 */
public class MethodRelationStream {

    Stream<MethodRelation> stream;

    public MethodRelationStream(Stream<MethodRelation> stream) {
        this.stream = stream;
    }

    public MethodRelationStream filterTo(MethodIdentifier methodIdentifier) {
        return new MethodRelationStream(stream.filter(methodRelation -> methodRelation.toIs(methodIdentifier)));
    }

    public MethodDeclarations fromMethods() {
        return stream.map(MethodRelation::from)
                .collect(MethodDeclarations.collector());
    }
}
