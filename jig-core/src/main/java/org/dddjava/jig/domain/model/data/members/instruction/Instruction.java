package org.dddjava.jig.domain.model.data.members.instruction;

/// コードで実行される命令
/// 現在、命令に共通のドメインロジックはないためメソッドを持たないが、
/// [Instructions] で扱えるものを制限するためにマーカーインタフェースとして置く。
public sealed interface Instruction
        permits ClassReference, FieldInstruction, InvokeDynamicInstruction, InvokedMethod, SimpleInstruction {
}
