package org.dddjava.jig.domain.model.knowledge.insight;

import org.dddjava.jig.domain.model.data.members.instruction.IfInstruction;
import org.dddjava.jig.domain.model.data.members.instruction.SimpleInstruction;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.information.members.JigMethod;

import static java.util.function.Predicate.not;

public record MethodInsight(JigMethod jigMethod) {
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
                .filter(methodCall -> methodCall.isNotJSL())
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
        return typeId().fqn();
    }

    public boolean smellOfNotUseMember() {
        if (jigMethod.isAbstract()) {
            // 抽象メソッドは対象外とする
            return false;
        }
        var typeId = jigMethod.declaringType();
        var instructions = jigMethod.instructions();
        if (instructions.fieldReferenceStream()
                .anyMatch(fieldReference -> fieldReference.declaringTypeId().equals(typeId))) {
            return false;
        }
        if (jigMethod.usingMethods().invokedMethodStream()
                .filter(methodCall -> !methodCall.isLambda()) // lambdaの合成メソッドのownerは自身になるので除外しないと「使用している」になってしまう
                .anyMatch(methodCall -> methodCall.methodOwner().equals(typeId))) {
            return false;
        }
        return true;
    }

    public boolean smellOfPrimitiveInterface() {
        if (jigMethod.isRecordComponent()) {
            // componentメソッドであれば基本型の授受を許容する
            return false;
        }

        return jigMethod.methodReturnTypeReference().id().isPrimitive()
                || jigMethod.parameterTypeStream()
                .anyMatch(jigTypeReference -> jigTypeReference.id().isPrimitive());
    }

    public boolean smellOfReferenceNull() {
        return jigMethod.instructions().containsSimpleInstruction(SimpleInstruction.NULL参照);
    }

    public boolean smellOfNullDecision() {
        return jigMethod.instructions().containsAny(instruction -> {
            if (instruction instanceof IfInstruction ifInstruction) {
                return ifInstruction.kind() == IfInstruction.Kind.NULL判定;
            }
            return false;
        });
    }

    public boolean smellOfReturnsBoolean() {
        return jigMethod.methodReturnTypeReference().id().isBoolean();
    }

    public boolean smellOfReturnsVoid() {
        return jigMethod.methodReturnTypeReference().id().isVoid();
    }
}
