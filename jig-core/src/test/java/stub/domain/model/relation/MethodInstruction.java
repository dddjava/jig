package stub.domain.model.relation;

import stub.domain.model.relation.method.Foo;
import stub.domain.model.relation.method.*;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class MethodInstruction {

    @MethodAnnotation
    void method(MethodArgument methodArgument) {
        // ローカル変数宣言だけ
        LocalValue localValue = null;

        // メソッド呼び出し
        Foo foo = null;
        foo.toBar().toBaz();
    }

    void fieldRef() {
        // 別クラスのフィールドを参照する
        Object obj = ReferenceConstantOwnerInMethod.FIELD;
    }

    void lambda() {
        Stream.empty()
                .forEach(item -> {
                    // Lambdaの中でだけ使用しているクラス
                    new UseInLambda();
                });
    }

    void methodRef() {
        // メソッド参照
        Function<MethodReference, String> method = MethodReference::toString;
    }

    MethodReturn method(List<ArgumentGenericsParameter> list) throws FugaException {
        new Instantiation();
        return null;
    }

    void nestedClass() {
        new EnclosedClass.NestedClass();
    }

    void causeException() throws ThrowingException {
        throw new ThrowingException();
    }
}
