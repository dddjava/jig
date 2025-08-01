package org.dddjava.jig.domain.model.knowledge.smell;

import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.types.JigType;

import java.util.Optional;

/**
 * メソッドの不吉なにおい
 */
public record MethodSmell(JigMethod method, MethodWorries methodWorries, JigType declaringJigType) {

    public static Optional<MethodSmell> createMethodSmell(JigMethod method, JigType declaringJigType) {
        // java.lang.Object由来は除外する
        if (method.isObjectMethod()) {
            return Optional.empty();
        }
        var methodWorries = MethodWorries.from(method, declaringJigType);
        if (methodWorries.empty()) return Optional.empty();

        var instance = new MethodSmell(method, methodWorries, declaringJigType);
        if (!instance.hasSmell()) return Optional.empty();
        return Optional.of(instance);
    }

    public TypeId methodReturnType() {
        return method().methodReturnTypeReference().id();
    }

    public boolean notUseMember() {
        return methodWorries.contains(MethodWorry.メンバを使用していない);
    }

    public boolean primitiveInterface() {
        if (method.isRecordComponent()) {
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
