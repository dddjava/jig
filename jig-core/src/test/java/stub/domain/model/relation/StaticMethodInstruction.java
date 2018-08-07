package stub.domain.model.relation;

import stub.domain.model.relation.constant.to_primitive_constant.ConstantFieldHolder;
import stub.domain.model.relation.constant.to_primitive_wrapper_constant.IntegerConstantFieldHolder;
import stub.domain.model.relation.method.*;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class StaticMethodInstruction {

    static InstructionField instructionField = null;

    @MethodAnnotation
    static void method(MethodArgument methodArgument) throws Exception {
        // ローカル変数宣言だけ
        LocalValue localValue = null;

        // メソッド呼び出し
        instructionField.invokeMethod().chainedInvokeMethod();
    }

    static void fieldRef() {
        // 別クラスのフィールドを参照する
        Object obj = ReferenceConstantOwnerInMethod.FIELD;
    }

    static void lambda() {
        Stream.empty()
                .forEach(item -> {
                    // Lambdaの中でだけ使用しているクラス
                    new UseInLambda();
                });
    }

    static void methodRef() {
        // メソッド参照
        Function<MethodReference, String> method = MethodReference::toString;
    }

    static MethodReturn method(List<ArgumentGenericsParameter> list) throws CheckedException {
        new Instantiation();
        return null;
    }

    static void nestedClass() {
        new EnclosedClass.NestedClass();
    }

    static void causeException() {
        throw new UncheckedExceptionA();
    }


    static void accessPrimitiveConstantField() {
        Object obj = ConstantFieldHolder.INT_CONSTANT;
    }

    static void accessStringConstantField() {
        Object obj = ConstantFieldHolder.STRING_CONSTANT;
    }

    static void accessPrimitiveWrapperConstantField() {
        Object obj = IntegerConstantFieldHolder.INTEGER_CONSTANT;
    }
}
