package org.dddjava.jig.domain.model.data.members.methods;

import org.dddjava.jig.domain.model.data.types.TypeId;

import java.util.List;

/**
 * Java言語で記述したメソッドの名前と引数部分。
 *
 * Java言語仕様のMethodDeclaratorは `Identifier(FormalParameterList)` であり、メソッド名と引数リストを指す。
 * {@link JigMethodId} は引数型もFQNで持ち、そちらが正しいが、javaソースコードはコンパイル時の環境によってFQNは変わりうる。
 * JigMethodIdを使用すると誤った突き合わせをする可能性があるため、別の型としている。
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jls/se17/html/jls-8.html#jls-8.4">JLS/Chapter 8. Classes/8.4. Method Declarations</a>
 */
public record JavaMethodDeclarator(
        TypeId typeId,
        String identifier,
        List<String> formalParameterList
) {

    /**
     * メソッド定義の文字列表現
     *
     * "asText()" とか。
     */
    public String asText() {
        return identifier + "(" + String.join(",", formalParameterList) + ")";
    }
}
