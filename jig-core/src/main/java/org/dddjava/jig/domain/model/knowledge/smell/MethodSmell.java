package org.dddjava.jig.domain.model.knowledge.smell;

import org.dddjava.jig.domain.model.data.classes.method.JigMethod;
import org.dddjava.jig.domain.model.data.classes.method.MethodDeclaration;
import org.dddjava.jig.domain.model.data.classes.method.MethodDerivation;

import java.util.Optional;

/**
 * メソッドの不吉なにおい
 */
public class MethodSmell {

    JigMethod method;
    boolean hasFieldClass;

    private MethodSmell(JigMethod method, boolean hasFieldClass) {
        this.method = method;
        this.hasFieldClass = hasFieldClass;
    }

    public static Optional<MethodSmell> createMethodSmell(JigMethod method, boolean hasFieldClass) {
        var instance = new MethodSmell(method, hasFieldClass);
        if (!instance.hasSmell()) return Optional.empty();
        return Optional.of(instance);
    }

    public MethodDeclaration methodDeclaration() {
        return method.declaration();
    }

    public boolean notUseMember() {
        // フィールド無しはクラスのスメル？
        if (!hasFieldClass) {
            return new MethodWorries(method).contains(MethodWorry.メンバを使用していない);
        }
        return false;
    }

    public boolean primitiveInterface() {
        if (method.derivation() == MethodDerivation.RECORD_COMPONENT) {
            // componentメソッドであれば基本型の授受を許容する
            return false;
        }

        return new MethodWorries(method).contains(MethodWorry.基本型の授受を行なっている);
    }

    public boolean returnsBoolean() {
        return new MethodWorries(method).contains(MethodWorry.真偽値を返している);
    }

    private boolean hasSmell() {
        if (method.objectMethod()) {
            // java.lang.Object由来は除外する
            return false;
        }

        // TODO このメソッドの並びと各実装がダメな感じなのでなんとかする。
        // 現状はここにメソッド追加するのと、列挙に追加するのと、判定メソッド作るのと、やってる。
        return notUseMember() || primitiveInterface() || returnsBoolean() || referenceNull() || nullDecision() || returnsVoid();
    }

    public boolean referenceNull() {
        return new MethodWorries(method).contains(MethodWorry.NULLリテラルを使用している);
    }

    public boolean nullDecision() {
        return new MethodWorries(method).contains(MethodWorry.NULL判定をしている);
    }

    public boolean returnsVoid() {
        return new MethodWorries(method).contains(MethodWorry.voidを返している);
    }
}
