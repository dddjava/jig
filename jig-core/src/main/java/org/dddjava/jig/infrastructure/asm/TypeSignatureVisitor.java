package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.parts.classes.type.ParameterizedType;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;
import org.objectweb.asm.signature.SignatureVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TypeSignature = visitBaseType
 * | visitTypeVariable
 * | visitArrayType
 * | ( visitClassType visitTypeArgument* ( visitInnerClassType visitTypeArgument* )* visitEnd ) )
 */
class TypeSignatureVisitor extends SignatureVisitor {
    public TypeSignatureVisitor(int api) {
        super(api);
    }

    private String className;
    private final List<String> typeArgumentClassName = new ArrayList<>();

    @Override
    public void visitClassType(String name) {
        className = name;
    }

    @Override
    public void visitTypeArgument() {
        // <?> などで指定された場合。シグネチャでは * となる。
        // 特に処理はしないがこのメソッドが何かのコメントのためにオーバーライドしておく。
        super.visitTypeArgument();
    }

    @Override
    public SignatureVisitor visitTypeArgument(char wildcard) {
        // wildcardは '+', '-' or '='.
        // 境界型を使用しない場合は = になる。
        // 一旦考慮しないことにする

        return new SignatureVisitor(this.api) {

            @Override
            public void visitClassType(String name) {
                typeArgumentClassName.add(name);
            }

            @Override
            public SignatureVisitor visitTypeArgument(char wildcard) {
                // 二段階以上のジェネリクスは無視する
                // デフォルトが return this のため、
                // このメソッドをオーバーライドしないと List<Map<String, String>> のパラメタ型が Map,String,String の3つ並列になってしまう。
                // 現在は Map のみ取れれば良いとして、ここでは上書き。
                return new SignatureVisitor(this.api) {
                };
            }
        };
    }

    public ParameterizedType generateParameterizedType() {
        return new ParameterizedType(
                new TypeIdentifier(className),
                typeArgumentClassName.stream().map(TypeIdentifier::new).collect(Collectors.toList())
        );
    }
}
