package stub.domain.model.relation;

import stub.domain.model.relation.constant.to_primitive_constant.ConstantFieldHolder;
import stub.domain.model.relation.constant.to_primitive_wrapper_constant.IntegerConstantFieldHolder;
import stub.domain.model.relation.method.*;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class ConstructorInstruction {

    InstructionField instructionField = null;

    @MethodAnnotation
    ConstructorInstruction(MethodArgument methodArgument) throws Exception {
        // メソッド呼び出し
        instructionField.invokeMethod().chainedInvokeMethod();

        // ローカル変数宣言だけ
        LocalValue localValue = null;

        // 別クラスのフィールドを参照する
        Object obj = ReferenceConstantOwnerInMethod.FIELD;

        Stream.empty()
                .forEach(item -> {
                    // Lambdaの中でだけ使用しているクラス
                    new UseInLambda();
                });

        // メソッド参照
        Function<MethodReference, String> method = MethodReference::toString;

        //primitive定数へのアクセス
        Object intConstant = ConstantFieldHolder.INT_CONSTANT;

        //String定数へのアクセス
        Object stringConstant = ConstantFieldHolder.STRING_CONSTANT;

        //Wrapper定数へのアクセス
        Object integerConstant = IntegerConstantFieldHolder.INTEGER_CONSTANT;

        // コンストラクタ呼び出し
        new Instantiation();

        // ネストクラスのコンストラクタ呼び出し
        new EnclosedClass.NestedClass();

        // 例外のスロー
        throw new UncheckedExceptionA();

    }

    ConstructorInstruction(List<ArgumentGenericsParameter> list) throws CheckedException {
    }

    // 同じテストを通したいので使用するクラスに挙がるようにここに記述しておく
    MethodReturn dummy;
}
