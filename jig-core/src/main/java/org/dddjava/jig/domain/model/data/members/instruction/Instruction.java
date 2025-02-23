package org.dddjava.jig.domain.model.data.members.instruction;

import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

import java.util.stream.Stream;

/// コードで実行される命令
public sealed interface Instruction
        permits BasicInstruction, ClassReference, DynamicMethodCall, FieldAccess, MethodCall, LambdaExpressionCall {

    /// メソッド呼び出しの場合に中身がある
    /// Optionalのほうがいいんだけど、Optionalからstreamへの変換がノイジーなのでstreamにしておく。
    default Stream<MethodCall> findMethodCall() {
        return Stream.empty();
    }

    /// この命令によって関連づけられる型のストリーム
    /// BasicInstruction以外ではあるはず。
    default Stream<TypeIdentifier> streamAssociatedTypes() {
        return Stream.empty();
    }
}
