package org.dddjava.jig.domain.model.knowledge.insight;

import org.dddjava.jig.domain.model.information.members.JigMethod;

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

    public int numberOfUsingClasses() {
        return Math.toIntExact(jigMethod.usingTypes()
                // この除外はusingのほうに持って行った方がいい気はする
                .values()
                .stream()
                .filter(typeId -> typeId.isJavaLanguageType())
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
}
