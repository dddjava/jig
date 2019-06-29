package org.dddjava.jig.domain.model.smells;

import org.dddjava.jig.domain.model.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.fact.relation.method.CalleeMethod;
import org.dddjava.jig.domain.model.richmethod.Method;
import org.dddjava.jig.domain.model.richmethod.MethodWorry;
import org.dddjava.jig.domain.model.fact.relation.method.CallerMethods;
import org.dddjava.jig.domain.model.fact.relation.method.MethodRelations;

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
        this.callerMethods = toMeRelation.callerMethodsOf(new CalleeMethod(method.declaration()));
    }

    public MethodDeclaration methodDeclaration() {
        return method.declaration();
    }

    public boolean notUseField() {
        return !fieldDeclarations.list().isEmpty() &&
                method.usingFields().empty();
    }

    public boolean primitiveInterface() {
        return method.methodWorries().contains(MethodWorry.基本型の授受を行なっている);
    }

    public boolean returnsBoolean() {
        return method.methodWorries().contains(MethodWorry.真偽値を返している);
    }

    public boolean hasSmell() {
        return notUseField() || primitiveInterface() || returnsBoolean() || referenceNull();
    }

    public boolean referenceNull() {
        return method.methodWorries().contains(MethodWorry.NULLリテラルを使用している);
    }

    public boolean nullDecision() {
        return method.methodWorries().contains(MethodWorry.NULL判定をしている);
    }

    public CallerMethods callerMethods() {
        return callerMethods;
    }
}
