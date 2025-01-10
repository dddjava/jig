package org.dddjava.jig.domain.model.knowledge.smell;

import org.dddjava.jig.domain.model.data.classes.field.FieldDeclarations;
import org.dddjava.jig.domain.model.data.classes.method.CallerMethods;
import org.dddjava.jig.domain.model.data.classes.method.MethodDeclaration;
import org.dddjava.jig.domain.model.data.classes.method.MethodDerivation;
import org.dddjava.jig.domain.model.data.classes.method.MethodRelations;
import org.dddjava.jig.domain.model.information.jigobject.member.JigMethod;

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
        if (method.derivation() == MethodDerivation.RECORD_COMPONENT) {
            // componentメソッドであれば基本型の授受を許容する
            return false;
        }

        return method.methodWorries().contains(MethodWorry.基本型の授受を行なっている);
    }

    public boolean returnsBoolean() {
        return method.methodWorries().contains(MethodWorry.真偽値を返している);
    }

    public boolean hasSmell() {
        if (method.objectMethod()) {
            // java.lang.Object由来は除外する
            return false;
        }

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
