package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.data.types.JigBaseTypeDataBundle;
import org.dddjava.jig.domain.model.data.types.JigTypeArgument;
import org.dddjava.jig.domain.model.data.types.JigTypeParameter;
import org.objectweb.asm.signature.SignatureVisitor;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * JVMSのシグネチャのうち、メソッドシグネチャから情報を取得するSignatureVisitorの実装
 *
 * ```
 * ClassSignature = ( visitFormalTypeParameter visitClassBound? visitInterfaceBound* )* (visitSuperclass visitInterface* )
 * ```
 *
 * ClassSignatureは型パラメタ、親クラス、インタフェースからなる。
 * 親クラスとインタフェースはさらに型引数をとることがある。
 * このクラスではクラス自身の型パラメタを扱い、親クラスやインタフェースはそれぞれでAsmTypeSignatureVisitorを生成して扱う。
 *
 * なお、ClassSignatureのvisitEndは呼ばれない。なぜかは調べていない。
 *
 * 例: {@code <T:Ljava/lang/Number;>LParentClass;Ljava/lang/Comparable<TT;>;}
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se21/html/jvms-4.html#jvms-4.7.9.1-400">JVMS 4.7.9.1-400</a>
 */
class AsmClassSignatureVisitor extends SignatureVisitor {
    private static final Logger logger = getLogger(AsmClassSignatureVisitor.class);

    /**
     * 型パラメタは複数あるので、それを取りまとめるための構造体
     */
    record JigTypeParameterBuilder(String name,
                                   List<AsmTypeSignatureVisitor> classBound,
                                   List<AsmTypeSignatureVisitor> interfaceBounds) {
        JigTypeParameterBuilder(String name) {
            this(name, new ArrayList<>(), new ArrayList<>());
        }

        JigTypeParameter build() {
            List<JigTypeArgument> bounds = Stream.concat(classBound.stream(), interfaceBounds.stream())
                    .map(AsmTypeSignatureVisitor::typeArgument)
                    .toList();

            return new JigTypeParameter(name, bounds);
        }
    }

    private final List<JigTypeParameterBuilder> jigTypeParameterBuilders = new ArrayList<>();
    private transient JigTypeParameterBuilder currentJigTypeParameterBuilder;

    private AsmTypeSignatureVisitor superclassAsmTypeSignatureVisitor;
    private final List<AsmTypeSignatureVisitor> interfaceAsmTypeSignatureVisitors = new ArrayList<>();

    protected AsmClassSignatureVisitor(int api) {
        super(api);
    }

    @Override
    public void visitFormalTypeParameter(String name) {
        logger.debug("visitFormalTypeParameter:{}", name);
        jigTypeParameterBuilders.add(currentJigTypeParameterBuilder = new JigTypeParameterBuilder(name));
    }

    @Override
    public SignatureVisitor visitClassBound() {
        logger.debug("visitClassBound");
        AsmTypeSignatureVisitor visitor = new AsmTypeSignatureVisitor(this.api);
        List<AsmTypeSignatureVisitor> list = currentJigTypeParameterBuilder.classBound();
        if (!list.isEmpty()) {
            throw new IllegalStateException("1つのTypeParameterに複数のClassBoundが存在する？？");
        }
        list.add(visitor);
        return visitor;
    }

    @Override
    public SignatureVisitor visitInterfaceBound() {
        logger.debug("visitInterfaceBound");
        AsmTypeSignatureVisitor visitor = new AsmTypeSignatureVisitor(this.api);
        currentJigTypeParameterBuilder.interfaceBounds().add(visitor);
        return visitor;
    }

    @Override
    public SignatureVisitor visitSuperclass() {
        logger.debug("visitSuperclass");
        AsmTypeSignatureVisitor typeSignatureVisitor = new AsmTypeSignatureVisitor(this.api);
        superclassAsmTypeSignatureVisitor = typeSignatureVisitor;
        return typeSignatureVisitor;
    }

    @Override
    public SignatureVisitor visitInterface() {
        logger.debug("visitInterface");
        AsmTypeSignatureVisitor typeSignatureVisitor = new AsmTypeSignatureVisitor(this.api);
        interfaceAsmTypeSignatureVisitors.add(typeSignatureVisitor);
        return typeSignatureVisitor;
    }

    public List<JigTypeParameter> jigTypeParameters() {
        return jigTypeParameterBuilders.stream()
                .map(JigTypeParameterBuilder::build)
                .toList();
    }

    public JigBaseTypeDataBundle jigBaseTypeDataBundle() {
        return new JigBaseTypeDataBundle(
                Optional.ofNullable(superclassAsmTypeSignatureVisitor).map(AsmTypeSignatureVisitor::jigTypeReference),
                interfaceAsmTypeSignatureVisitors.stream()
                        .map(AsmTypeSignatureVisitor::jigTypeReference)
                        .toList()
        );
    }

}
