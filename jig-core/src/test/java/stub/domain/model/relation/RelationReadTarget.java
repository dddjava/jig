package stub.domain.model.relation;

import stub.domain.model.relation.foo.Baz;
import stub.domain.model.relation.foo.Foo;
import stub.domain.model.relation.qux.Qux;
import stub.domain.model.relation.test.*;

import java.util.List;
import java.util.function.Function;

/**
 * 使用しているクラスを抽出するテスト対象
 */
public class RelationReadTarget {
    // フィールド
    Foo foo;

    // 配列
    ArrayField[] array;
    // ジェネリクス
    List<GenericsField> list;

    // 戻り値と例外
    Baz foo() throws FugaException {
        return foo
                // メソッドチェーンの途中で使う
                .toBar()
                .toBaz();
    }

    // 引数
    void qux(MethodArgument methodArgument1, MethodArgument methodArgument2) {
        Function<Void, Void> function = (param) -> {
            // Lambdaの中でだけ使用しているクラス
            new UseInLambda();
            System.out.println("HOGERA");
            return null;
        };
        function.apply(null);
        // ネストされたクラス
        new Qux.Quuz();
    }
}
