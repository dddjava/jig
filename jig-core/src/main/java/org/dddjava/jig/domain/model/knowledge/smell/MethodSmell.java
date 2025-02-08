package org.dddjava.jig.domain.model.knowledge.smell;

import org.dddjava.jig.domain.model.data.classes.method.JigMethod;
import org.dddjava.jig.domain.model.data.classes.method.MethodDeclaration;
import org.dddjava.jig.domain.model.data.classes.method.MethodDerivation;
import org.dddjava.jig.domain.model.information.type.JigType;

import java.util.Optional;

/**
 * メソッドの不吉なにおい
 */
public record MethodSmell(JigMethod method, MethodWorries methodWorries) {

    public static Optional<MethodSmell> createMethodSmell(JigMethod method, JigType contextJigType) {
        // java.lang.Object由来は除外する
        if (method.objectMethod()) {
            return Optional.empty();
        }
        var methodWorries = MethodWorries.from(method, contextJigType);
        if (methodWorries.empty()) return Optional.empty();

        var instance = new MethodSmell(method, methodWorries);
        if (!instance.hasSmell()) return Optional.empty();
        return Optional.of(instance);
    }

    public MethodDeclaration methodDeclaration() {
        return method.declaration();
    }

    public boolean notUseMember() {
        return methodWorries.contains(MethodWorry.メンバを使用していない);
    }

    public boolean primitiveInterface() {
        if (method.derivation() == MethodDerivation.RECORD_COMPONENT) {
            // componentメソッドであれば基本型の授受を許容する
            return false;
        }

        return methodWorries.contains(MethodWorry.基本型の授受を行なっている);
    }

    public boolean returnsBoolean() {
        return methodWorries.contains(MethodWorry.真偽値を返している);
    }

    private boolean hasSmell() {

        // TODO このメソッドの並びと各実装がダメな感じなのでなんとかする。
        // 現状はここにメソッド追加するのと、列挙に追加するのと、判定メソッド作るのと、やってる。
        return notUseMember() || primitiveInterface() || returnsBoolean() || referenceNull() || nullDecision() || returnsVoid();
    }

    public boolean referenceNull() {
        return methodWorries.contains(MethodWorry.NULLリテラルを使用している);
    }

    public boolean nullDecision() {
        return methodWorries.contains(MethodWorry.NULL判定をしている);
    }

    public boolean returnsVoid() {
        return methodWorries.contains(MethodWorry.voidを返している);
    }
}
