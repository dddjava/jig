package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.data.types.TypeId;
import org.objectweb.asm.Type;

import java.util.regex.Pattern;

/**
 * JVMバイトコード上の型名
 *
 * バイトコードでは型名が以下のような形式で表現される:
 * - バイナリ名: {@code org/dddjava/jig/...} （スラッシュ区切り）
 * - 配列記法: {@code [Ljava/lang/String;} など
 * - コンパイラ生成の匿名クラス: {@code Hoge$1} など
 *
 * このクラスはこれらの生のJVM型名をラップし、ドメインの {@link TypeId} への変換を担う。
 */
record JvmTypeName(String value) {

    private static final Pattern COMPILER_GENERATED_SUFFIX = Pattern.compile("\\$\\d+$");

    /**
     * JVMバイナリ名（スラッシュ区切り）からJvmTypeNameを生成する。
     */
    static JvmTypeName fromBinaryName(String jvmBinaryName) {
        return new JvmTypeName(jvmBinaryName.replace('/', '.'));
    }

    /**
     * ASMの {@link Type} からJvmTypeNameを生成する。
     */
    static JvmTypeName fromAsmType(Type asmType) {
        return new JvmTypeName(asmType.getClassName());
    }

    /**
     * ドメインの {@link TypeId} に変換する。
     * コンパイラ生成の匿名クラス名（{@code $digit} サフィックス）は正規化する。
     */
    TypeId toTypeId() {
        return TypeId.valueOf(COMPILER_GENERATED_SUFFIX.matcher(value).replaceFirst(""));
    }
}
