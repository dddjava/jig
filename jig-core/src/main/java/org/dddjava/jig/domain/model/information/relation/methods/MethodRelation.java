package org.dddjava.jig.domain.model.information.relation.methods;

import org.dddjava.jig.domain.model.data.members.methods.JigMethodIdentifier;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.information.relation.graph.Edge;

/**
 * メソッドの使用しているメソッド
 */
public record MethodRelation(Edge<JigMethodIdentifier> edge) {

    public static MethodRelation from(JigMethodIdentifier from, JigMethodIdentifier to) {
        return new MethodRelation(new Edge<>(from, to));
    }

    public JigMethodIdentifier from() {
        return edge.from();
    }

    public JigMethodIdentifier to() {
        return edge.to();
    }

    public boolean calleeMethodIs(JigMethodIdentifier jigMethodIdentifier) {
        return to().equals(jigMethodIdentifier);
    }

    public TypeIdentifier toType() {
        return to().tuple().declaringTypeIdentifier();
    }

    public TypeIdentifier fromType() {
        return from().tuple().declaringTypeIdentifier();
    }
}
