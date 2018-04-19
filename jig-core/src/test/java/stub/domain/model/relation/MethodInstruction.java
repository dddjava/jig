package stub.domain.model.relation;

import stub.domain.model.relation.foo.Foo;
import stub.domain.model.relation.test.*;

import java.util.List;

public class MethodInstruction {

    @RetentionClassAnnotation
    void method(MethodArgument methodArgument) {
        // ローカル変数宣言だけ
        LocalValue localValue = null;

        // メソッド呼び出し
        Foo foo = null;
        foo.toBar().toBaz();
    }

    MethodReturn method(List<GenericArgument> list) throws FugaException {
        new Instantiation();
        return null;
    }

    void fieldReference() {
        // 別クラスのフィールドを参照する
        Object obj = FieldReference.FIELD;
    }
}
