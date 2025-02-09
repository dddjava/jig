package org.dddjava.jig.domain.model.knowledge.smell;

import java.util.UUID;

class MemberUsageCheckNormalClass {
    static int staticField = 0;
    int instanceField = 0;

    void インスタンスフィールドを使用しているインスタンスメソッド() {
        instanceField = 1;
    }

    void staticフィールドを使用しているインスタンスメソッド() {
        staticField = 1;
    }

    void 何も使用していないインスタンスメソッド() {
    }

    void 他クラスのメソッドを使用しているがメンバを使用していないメソッド() {
        var uuid = UUID.randomUUID();
    }

    void インスタンスメソッドを使用しているインスタンスメソッド() {
        何も使用していないインスタンスメソッド();
    }

    void staticメソッドを使用しているインスタンスメソッド() {
        何も使用していないstaticメソッド();
    }


    static void staticフィールドを使用しているstaticメソッド() {
        staticField = 1;
    }

    static void 何も使用していないstaticメソッド() {
    }
}

interface MemberUsageCheckInterface {
    void インタフェースのメソッド();

    default void インタフェースのdefaultメソッドでメンバを使用していない() {
    }

    default void インタフェースのdefaultメソッドでインタフェースのメソッドを使用している() {
        インタフェースのメソッド();
    }
}

enum MemberUsageCheckEnum {
    ENUM_CONSTANT_1,
    ENUM_CONSTANT_2;

    int field;

    void 何も使用していないメソッド() {
    }

    void 列挙定数を使用しているメソッド() {
        var name = ENUM_CONSTANT_2.name();
    }

    int インスタンスフィールドを参照しているメソッド() {
        return field;
    }

    static MemberUsageCheckEnum ファクトリメソッド1(String name) {
        return ENUM_CONSTANT_1;
    }

    static MemberUsageCheckEnum ファクトリメソッド2(String name) {
        return valueOf(name);
    }
}

record MemberUsageCheckRecord(String componentA, String componentB) {

    void 何も使用していないメソッド() {
    }

    String コンポーネントフィールドを呼び出しているメソッド() {
        return componentA;
    }

    String コンポーネントメソッドを呼び出しているメソッド() {
        return componentA();
    }

    public String componentB() {
        // コンポーネントメソッドをオーバーライドしつつ何にもアクセスしなくなったもの
        return null;
    }
}
