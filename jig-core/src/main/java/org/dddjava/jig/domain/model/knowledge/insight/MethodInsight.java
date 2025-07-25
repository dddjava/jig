package org.dddjava.jig.domain.model.knowledge.insight;

import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.information.members.JigMethod;

import static java.util.function.Predicate.not;

public record MethodInsight(
        JigMethod jigMethod
) {
    public String fqn() {
        return jigMethod.fqn();
    }

    public String label() {
        return jigMethod.labelText();
    }

    public int cyclomaticComplexity() {
        return jigMethod.instructions().cyclomaticComplexity();
    }

    public int numberOfUsingTypes() {
        return Math.toIntExact(jigMethod.usingTypes()
                // この除外はusingのほうに持って行った方がいい気はする
                .values()
                .stream()
                .filter(not(TypeId::isJavaLanguageType))
                .count());
    }

    public int numberOfUsingMethods() {
        return Math.toIntExact(jigMethod.usingMethods().invokedMethodStream()
                // この除外はusingのほうに持って行った方がいい気はする
                .filter(methodCall -> !methodCall.isJSL())
                .distinct()
                .count());
    }

    public int numberOfUsingFields() {
        // lambdaが展開できていない
        return jigMethod.usingFields().jigFieldIds().size();
    }

    public int size() {
        // lambdaが展開できていない
        return jigMethod.instructions().instructions().size();
    }

    public TypeId typeId() {
        return jigMethod.declaringType();
    }

    public String packageFqn() {
        return typeId().packageId().asText();
    }

    public String typeFqn() {
        return typeId().fullQualifiedName();
    }
}
