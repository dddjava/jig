package org.dddjava.jig.domain.model.data.classes.method;

import org.dddjava.jig.domain.model.data.classes.method.instruction.MethodInstructions;
import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;

import java.util.List;
import java.util.Set;

/**
 * メソッドが依存しているもの
 */
public class MethodDepend {

    private final MethodInstructions methodInstructions;

    public MethodDepend(MethodInstructions methodInstructions) {
        this.methodInstructions = methodInstructions;
    }

    public UsingFields usingFields() {
        return new UsingFields(methodInstructions.fieldReferences());
    }

    public UsingMethods usingMethods() {
        return new UsingMethods(methodInstructions.instructMethods());
    }

    public List<MethodDeclaration> methodInstructions() {
        // usingMethodsとかぶってるような
        return methodInstructions.instructMethods().list();
    }

    public boolean notUseMember() {
        return methodInstructions.hasMemberInstruction();
        // TODO 元の判定誤ってる気がする。メンバを使用していない判定で、他インスタンスのメンバの使用でセーフになってそう。
        // return usingFields.isEmpty() && methodInstructions.isEmpty();
    }

    public boolean hasNullReference() {
        return methodInstructions.hasNullReference();
    }

    public Set<TypeIdentifier> collectUsingTypes() {
        return methodInstructions.usingTypes();
    }
}
