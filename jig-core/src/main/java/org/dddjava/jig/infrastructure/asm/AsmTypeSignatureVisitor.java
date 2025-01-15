package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.data.classes.type.ParameterizedType;
import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;
import org.objectweb.asm.signature.SignatureVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * TypeSignature = visitBaseType
 * | visitTypeVariable
 * | visitArrayType
 * | ( visitClassType visitTypeArgument* ( visitInnerClassType visitTypeArgument* )* visitEnd ) )
 */
class AsmTypeSignatureVisitor extends SignatureVisitor {
    private static final Logger logger = LoggerFactory.getLogger(AsmTypeSignatureVisitor.class);

    public AsmTypeSignatureVisitor(int api) {
        super(api);
    }

    private String className;
    private final List<AsmTypeSignatureVisitor> argumentAsmTypeSignatureVisitors = new ArrayList<>();

    @Override
    public void visitBaseType(char descriptor) {
        logger.debug("visitBaseType:{}", descriptor);
        className = switch (descriptor) {
            case 'Z' -> "boolean";
            case 'C' -> "char";
            case 'B' -> "byte";
            case 'S' -> "short";
            case 'I' -> "int";
            case 'F' -> "float";
            case 'J' -> "long";
            case 'D' -> "double";
            // methodのResultのときは（BaseTypeではないけど）ここに入ってくる
            case 'V' -> "void";
            default -> throw new IllegalArgumentException("%s is not base type".formatted(descriptor));
        };
    }

    /**
     * 型引数名。
     * nameはSignatureで T と ; に挟まれたもの。
     * - T: TT;
     * - T1: TT1;
     *
     * <pre><code>
     * TypeVariableSignature:
     *   T Identifier ;
     * </code></pre>
     */
    @Override
    public void visitTypeVariable(String name) {
        logger.debug("visitTypeVariable:{}", name);
        // TODO: TypeIdentifierではないんだよなぁ……
        this.className = name;
    }

    @Override
    public void visitClassType(String name) {
        logger.debug("visitClassType:{}", name);
        className = name;
    }

    @Override
    public void visitTypeArgument() {
        logger.debug("visitTypeArgument");
        // <?> などで指定された場合。シグネチャでは * となる。
        // 特に処理はしないがこのメソッドが何かのコメントのためにオーバーライドしておく。
        super.visitTypeArgument();
    }

    @Override
    public SignatureVisitor visitTypeArgument(char wildcard) {
        logger.debug("visitTypeArgument:{}", wildcard);
        // wildcardは '+', '-' or '='.
        // 境界型を使用しない場合は = になる。
        // 一旦考慮しないことにする

        var typeSignatureVisitor = new AsmTypeSignatureVisitor(this.api);
        argumentAsmTypeSignatureVisitors.add(typeSignatureVisitor);
        return typeSignatureVisitor;
    }

    @Override
    public void visitInnerClassType(String name) {
        logger.debug("visitInnerClassType:{}", name);
        super.visitInnerClassType(name);
    }

    @Override
    public void visitEnd() {
        logger.debug("visitEnd");
        super.visitEnd();
    }

    public ParameterizedType generateParameterizedType() {
        var argumentParameterizedTypes = argumentAsmTypeSignatureVisitors.stream()
                .map(AsmTypeSignatureVisitor::generateParameterizedType)
                .toList();
        return new ParameterizedType(TypeIdentifier.valueOf(className), argumentParameterizedTypes);
    }
}
