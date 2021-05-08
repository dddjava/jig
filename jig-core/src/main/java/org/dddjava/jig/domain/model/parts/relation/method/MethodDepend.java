package org.dddjava.jig.domain.model.parts.relation.method;

import org.dddjava.jig.domain.model.parts.classes.field.FieldDeclaration;
import org.dddjava.jig.domain.model.parts.classes.field.FieldDeclarations;
import org.dddjava.jig.domain.model.parts.classes.method.MethodDeclaration;
import org.dddjava.jig.domain.model.parts.classes.method.MethodDeclarations;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifiers;

import java.util.*;

/**
 * メソッドが依存しているもの
 */
public class MethodDepend {

    Set<TypeIdentifier> usingTypes;

    // FIXME 取扱注意。AsmClassVisitorのvisitFieldInsnを参照。
    List<FieldDeclaration> usingFields;

    List<MethodDeclaration> usingMethods;
    boolean hasNullReference;

    public MethodDepend(Set<TypeIdentifier> usingTypes, List<FieldDeclaration> usingFields, List<MethodDeclaration> usingMethods, boolean hasNullReference) {
        this.usingTypes = usingTypes;
        this.usingFields = usingFields;
        this.usingMethods = usingMethods;
        this.hasNullReference = hasNullReference;
    }

    public UsingFields usingFields() {
        return new UsingFields(new FieldDeclarations(usingFields));
    }

    public UsingMethods usingMethods() {
        return new UsingMethods(usingMethods.stream().collect(MethodDeclarations.collector()));
    }

    public boolean notUseMember() {
        return usingFields.isEmpty() && usingMethods.isEmpty();
    }

    public boolean hasNullReference() {
        return hasNullReference;
    }

    public Collection<TypeIdentifier> collectUsingTypes() {
        Set<TypeIdentifier> typeIdentifiers = new HashSet<>(usingTypes);

        for (FieldDeclaration usingField : usingFields) {
            typeIdentifiers.add(usingField.declaringType());
            typeIdentifiers.add(usingField.typeIdentifier());
        }

        for (MethodDeclaration usingMethod : usingMethods) {
            // メソッドやコンストラクタの持ち主
            // new演算子で呼び出されるコンストラクタの持ち主をここで捕まえる
            typeIdentifiers.add(usingMethod.declaringType());

            // 呼び出したメソッドの戻り値の型
            typeIdentifiers.add(usingMethod.methodReturn().typeIdentifier());
        }

        return typeIdentifiers;
    }

    public TypeIdentifiers usingTypes() {
        return new TypeIdentifiers(new ArrayList<>(collectUsingTypes()));
    }
}
