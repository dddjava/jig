package stub.domain.model.relation;

import stub.domain.model.relation.constant.to_primitive_constant.IntConstantFieldHolder;
import stub.domain.model.relation.constant.to_primitive_wrapper_constant.IntegerConstantFieldHolder;
import stub.domain.model.relation.method.*;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class MethodInstruction {

    InstructionField instructionField = null;

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
        Function<MethodReference, String> method = MethodReference::toString;
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


    static void accessPrimitiveConstantField() {
        Object obj = IntConstantFieldHolder.INT_CONSTANT;
    }

    static void accessPrimitiveWrapperConstantField() {
        Object obj = IntegerConstantFieldHolder.INTEGER_CONSTANT;
    }
}
