package org.dddjava.jig.domain.model.data.classes.method;

import org.dddjava.jig.domain.model.data.classes.field.FieldDeclaration;
import org.dddjava.jig.domain.model.data.classes.field.FieldDeclarations;
import org.dddjava.jig.domain.model.data.classes.method.instruction.MethodInstructions;
import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifiers;

import java.util.*;

/**
 * メソッドが依存しているもの
 */
public class MethodDepend {

    private final MethodInstructions methodInstructions;
    Set<TypeIdentifier> usingTypes;

    // FIXME 取扱注意。AsmClassVisitorのvisitFieldInsnを参照。
    List<FieldDeclaration> usingFields;

    public MethodDepend(Set<TypeIdentifier> usingTypes, List<FieldDeclaration> usingFields, MethodInstructions methodInstructions) {
        this.usingTypes = usingTypes;
        this.usingFields = usingFields;
        this.methodInstructions = methodInstructions;
    }

    public UsingFields usingFields() {
        return new UsingFields(new FieldDeclarations(usingFields));
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

    public Collection<TypeIdentifier> collectUsingTypes() {
        Set<TypeIdentifier> typeIdentifiers = new HashSet<>(usingTypes);

        for (FieldDeclaration usingField : usingFields) {
            typeIdentifiers.add(usingField.declaringType());
            typeIdentifiers.add(usingField.typeIdentifier());
        }

        for (MethodDeclaration usingMethod : methodInstructions()) {
            // メソッドやコンストラクタの持ち主
            // new演算子で呼び出されるコンストラクタの持ち主をここで捕まえる
            typeIdentifiers.add(usingMethod.declaringType());

            // 呼び出したメソッドの戻り値の型
            typeIdentifiers.add(usingMethod.methodReturn().typeIdentifier());
        }

        typeIdentifiers.addAll(methodInstructions.usingTypes());

        return typeIdentifiers;
    }

    public TypeIdentifiers usingTypes() {
        return new TypeIdentifiers(new ArrayList<>(collectUsingTypes()));
    }
}
