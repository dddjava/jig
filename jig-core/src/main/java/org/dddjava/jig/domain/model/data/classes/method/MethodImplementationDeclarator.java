package org.dddjava.jig.domain.model.data.classes.method;

import java.util.List;

/**
 * コードとして記述した MethodDeclarator
 *
 * MethodDeclarator はJava言語仕様における Identifier(FormalParameterList) のことで、メソッド名と引数リストのこと。
 * {@link MethodDeclaration} や {@link MethodIdentifier}、 {@link MethodSignature} などは引数型のFQNを持つが、
 * javaソースコードはコンパイル時の環境によってFQNは変わりうるため、これらの型にすると誤用してしまうため別の型としている。
 *
 * https://docs.oracle.com/javase/specs/jls/se17/html/jls-8.html#jls-8.4
 */
public record MethodImplementationDeclarator(
        String identifier,
        List<String> formalParameterList
) {

    /**
     * "asText()" のような文字列
     */
    public String asText() {
        return identifier + "(" + String.join(",", formalParameterList) + ")";
    }

    /**
     * 一致する可能性があるかを判定する
     */
    public boolean possiblyMatches(MethodSignature methodSignature) {
        if (methodSignature.methodName().equals(identifier)) {
            // TODO 引数の型名を突き合わせる。
            // formalParameterListを受けられるようになったら対応。
            return true;
        }
        return false;
    }
}
