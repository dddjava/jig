package stub.domain.model.relation.tails;

import stub.domain.model.relation.constant.to_primitive_constant.ConstantFieldHolder;
import stub.domain.model.relation.constant.to_primitive_wrapper_constant.IntegerConstantFieldHolder;
import stub.domain.model.relation.method.*;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * パッケージ依存の検証のための呼び出し元となるクラス
 *
 * {@link org.dddjava.jig.application.PackageDependenciesTest}は主にこのクラスからの関連でパッケージ関連を作る。
 */
public class DependencyTails {

    InstructionField instructionField = null;

    void relateToClass() {

    }

    @MethodAnnotation
    void method(MethodArgument methodArgument) throws Exception {
        // ローカル変数宣言だけ
        LocalValue localValue = null;

        // メソッド呼び出し
        instructionField.invokeMethod().chainedInvokeMethod();
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
        Function<MethodReference, String> method = MethodReference::referenceMethod;
    }

    MethodReturn method(List<ArgumentGenericsParameter> list) throws CheckedException {
        new Instantiation();
        return null;
    }

    void nestedClass() {
        new EnclosedClass.NestedClass();
    }

    void causeException() {
        throw new UncheckedExceptionA();
    }

    void メソッド内のフィールド参照() {
        // 検出されない参照
        // プリミティブおよびStringはインライン化される
        Object int定数 = ConstantFieldHolder.INT_CONSTANT;
        Object String定数 = ConstantFieldHolder.STRING_CONSTANT;

        // 検出される参照
        Object obj = IntegerConstantFieldHolder.INTEGER_CONSTANT;
    }
}
