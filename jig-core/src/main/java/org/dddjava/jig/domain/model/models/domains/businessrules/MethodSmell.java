package org.dddjava.jig.domain.model.models.domains.businessrules;

import org.dddjava.jig.domain.model.models.jigobject.member.JigMethod;
import org.dddjava.jig.domain.model.models.jigobject.member.MethodWorry;
import org.dddjava.jig.domain.model.parts.classes.field.FieldDeclarations;
import org.dddjava.jig.domain.model.parts.classes.method.MethodDeclaration;
import org.dddjava.jig.domain.model.parts.relation.method.CallerMethods;
import org.dddjava.jig.domain.model.parts.relation.method.MethodRelations;

/**
 * メソッドの不吉なにおい
 */
public class MethodSmell {

    JigMethod method;
    FieldDeclarations fieldDeclarations;
    CallerMethods callerMethods;

    public MethodSmell(JigMethod method, FieldDeclarations fieldDeclarations, MethodRelations methodRelations) {
        this.method = method;
        this.fieldDeclarations = fieldDeclarations;
        this.callerMethods = methodRelations.callerMethodsOf(method.declaration());
    }

    public MethodDeclaration methodDeclaration() {
        return method.declaration();
    }

    public boolean notUseMember() {
        // フィールド無しはクラスのスメル？
        if (!fieldDeclarations.empty()) {
            return false;
        }
        return method.methodWorries().contains(MethodWorry.メンバを使用していない);
    }

    public boolean primitiveInterface() {
        return method.methodWorries().contains(MethodWorry.基本型の授受を行なっている);
    }

    public boolean returnsBoolean() {
        return method.methodWorries().contains(MethodWorry.真偽値を返している);
    }

    public boolean hasSmell() {
        // TODO このメソッドの並びと各実装がダメな感じなのでなんとかする。
        // 現状はここにメソッド追加するのと、列挙に追加するのと、判定メソッド作るのと、やってる。
        return notUseMember() || primitiveInterface() || returnsBoolean() || referenceNull() || nullDecision() || returnsVoid();
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

    public boolean returnsVoid() {
        return method.methodWorries().contains(MethodWorry.voidを返している);
    }
}
