package org.dddjava.jig.domain.model.information.relation.methods;

import org.dddjava.jig.domain.model.data.members.methods.JigMethodId;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.information.relation.graph.Edge;

/**
 * メソッドの使用しているメソッド
 */
public record MethodRelation(Edge<JigMethodId> edge) {

    public static MethodRelation from(JigMethodId from, JigMethodId to) {
        return new MethodRelation(Edge.of(from, to));
    }

    public JigMethodId from() {
        return edge.from();
    }

    public JigMethodId to() {
        return edge.to();
    }

    public boolean calleeMethodIs(JigMethodId jigMethodId) {
        return to().equals(jigMethodId);
    }

    public TypeId toType() {
        return to().tuple().declaringTypeIdentifier();
    }

    public TypeId fromType() {
        return from().tuple().declaringTypeIdentifier();
    }
}
