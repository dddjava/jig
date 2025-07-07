package org.dddjava.jig.domain.model.data.members.instruction;

import org.dddjava.jig.domain.model.data.types.TypeId;

import java.util.stream.Stream;

/**
 * コードで実行される命令
 */
public sealed interface Instruction
        permits BasicInstruction, ClassReference, DynamicMethodCall, FieldAccess,
        IfInstruction, SwitchInstruction,
        TryCatchInstruction,
        JumpTarget,
        LambdaExpressionCall, MethodCall {

    /**
     * メソッド呼び出しの場合に中身がある
     * Optionalのほうがいいんだけど、Optionalからstreamへの変換がノイジーなのでstreamにしておく。
     */
    default Stream<MethodCall> findMethodCall() {
        return Stream.empty();
    }

    /**
     * この命令によって関連づけられる型のストリーム
     */
    default Stream<TypeId> streamAssociatedTypes() {
        return Stream.empty();
    }

    /**
     * lambdaのメソッド呼び出しをインライン化したもの
     */
    default Stream<MethodCall> lambdaInlinedMethodCallStream() {
        return findMethodCall();
    }
}
