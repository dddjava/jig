package org.dddjava.jig.domain.model.angle.smells;

import org.dddjava.jig.domain.model.angle.unit.method.Method;
import org.dddjava.jig.domain.model.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.bytecode.CallerMethods;
import org.dddjava.jig.domain.model.networks.method.MethodRelations;

/**
 * メソッドの不吉なにおい
 */
public class MethodSmellAngle {

    Method method;
    FieldDeclarations fieldDeclarations;
    CallerMethods callerMethods;

    public MethodSmellAngle(Method method, FieldDeclarations fieldDeclarations, MethodRelations toMeRelation) {
        this.method = method;
        this.fieldDeclarations = fieldDeclarations;
        this.callerMethods = toMeRelation.callerMethodsOf(method.declaration());
    }

    public MethodDeclaration methodDeclaration() {
        return method.declaration();
    }

    public String decisionNumber() {
        return method.decisionNumber().asText();
    }

    public boolean notUseField() {
        return !fieldDeclarations.list().isEmpty() &&
                method.usingFields().empty();
    }

    public boolean primitiveInterface() {
        return method.declaration().methodReturn().isPrimitive()
                || method.declaration().methodSignature().arguments().stream().anyMatch(TypeIdentifier::isPrimitive);
    }

    public boolean returnsBoolean() {
        return method.declaration().methodReturn().typeIdentifier().isBoolean();
    }

    public boolean hasSmell() {
        return notUseField() || primitiveInterface() || method.decisionNumber().notZero() || returnsBoolean();
    }

    public CallerMethods callerMethods() {
        return callerMethods;
    }
}
