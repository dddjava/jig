package org.dddjava.jig.domain.model.data.classes.method;

import java.util.List;

/**
 * Java言語で記述したメソッドの名前と引数部分。
 *
 * Java言語仕様のMethodDeclaratorは `Identifier(FormalParameterList)` であり、メソッド名と引数リストを指す。
 * {@link MethodDeclaration} や {@link MethodIdentifier}、 {@link MethodSignature} などは引数型のFQNを持つが、
 * javaソースコードはコンパイル時の環境によってFQNは変わりうるため、これらの型にすると誤用してしまうため別の型としている。
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jls/se17/html/jls-8.html#jls-8.4">JLS/Chapter 8. Classes/8.4. Method Declarations</a>
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
            var parameterizedArgumentTypes = methodSignature.arguments();
            // 引数の数が一致しない場合は不一致と判断する。
            // TODO インナークラスではreceiverの存在により一致しない気がする。挙動を確認するテストを追加する必要がある。不一致で対応しないなら警告を出したり、どこかに制限事項として書く。
            if (parameterizedArgumentTypes.size() != formalParameterList.size()) return false;

            for (int i = 0; i < parameterizedArgumentTypes.size(); i++) {
                var parameterizedArgumentType = parameterizedArgumentTypes.get(i);
                var formalParameter = formalParameterList.get(i);
                // FQNが一致すればOK。
                // TODO 引数型がネストクラスの場合、コードで書かれるFQNは pkg.Hoge.Fuga となるのに対し、バイトコードでは pkg.Hoge$Fuga となり一致しない可能性がある。要テスト。
                if (formalParameter.equals(parameterizedArgumentType.typeIdentifier().fullQualifiedName())) {
                    continue;
                }
                // FQNが一致せずとも単純クラス名が一致すればOKとする。このマッチは確実ではなく、異なるものと一致する可能性がある。
                if (formalParameter.equals(parameterizedArgumentType.asSimpleText())) {
                    // 偶然一致してしまう可能性があるが、ほとんどの場合こちらになるので警告などは出さない。
                    // 処理的にはこちらを優先した方がいいんだろうなぁと思いつつ、精度の高い順に評価したいのでFQN一致の後に行う。
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
