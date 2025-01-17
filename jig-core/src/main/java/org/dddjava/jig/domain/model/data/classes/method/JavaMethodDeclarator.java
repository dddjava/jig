package org.dddjava.jig.domain.model.data.classes.method;

import java.util.List;

/**
 * Java言語で記述したメソッドの名前と引数部分。
 *
 * Java言語仕様のMethodDeclaratorは `Identifier(FormalParameterList)` であり、メソッド名と引数リストを指す。
 * {@link MethodDeclaration} や {@link MethodIdentifier}、 {@link MethodSignature} などは引数型のFQNを持つが、
 * javaソースコードはコンパイル時の環境によってFQNは変わりうるため、これらの型にすると誤用してしまうため別の型としている。
 *
 * https://docs.oracle.com/javase/specs/jls/se17/html/jls-8.html#jls-8.4
 */
public record JavaMethodDeclarator(
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
            var typeIdentifiers = methodSignature.listArgumentTypeIdentifiers();
            if (typeIdentifiers.size() != formalParameterList.size()) return false;

            for (int i = 0; i < typeIdentifiers.size(); i++) {
                var typeIdentifier = typeIdentifiers.get(i);
                var formalParameter = formalParameterList.get(i);
                // FQNが一致すればOK
                if (formalParameter.equals(typeIdentifier.fullQualifiedName())) {
                    continue;
                }
                // FQNが一致せずとも単純クラス名が一致すればOKとする。これでマッチした場合に変なことになる。
                if (formalParameter.equals(typeIdentifier.asSimpleText())) {
                    continue;
                }
                // 一致しないものが一つでもあったら不一致とみなす
                return false;
            }

            // 全パラメタのFQNもしくは単純名が一致した
            return true;
        }
        return false;
    }
}
