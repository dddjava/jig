package org.dddjava.jig.domain.model.knowledge.smell;

import java.util.UUID;

class MySut {
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

interface MySutInterface {
    void インタフェースのメソッド();

    default void インタフェースのdefaultメソッドでメンバを使用していない() {
    }

    default void インタフェースのdefaultメソッドでインタフェースのメソッドを使用している() {
        インタフェースのメソッド();
    }
}
