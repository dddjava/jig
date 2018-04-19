package stub.domain.model.relation;

import stub.domain.model.relation.foo.Foo;
import stub.domain.model.relation.test.LocalValue;

public class MethodInstruction {

    void method() {
        // ローカル変数宣言だけ
        LocalValue localValue = null;

        // メソッド呼び出し
        Foo foo = null;
        foo.toBar().toBaz();
    }
}
