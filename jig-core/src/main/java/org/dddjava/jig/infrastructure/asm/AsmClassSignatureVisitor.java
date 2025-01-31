package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.data.classes.type.ParameterizedType;
import org.objectweb.asm.signature.SignatureVisitor;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * ClassSignature = ( visitFormalTypeParameter visitClassBound? visitInterfaceBound* )* (visitSuperclass visitInterface* )
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-4.html#jvms-4.7.9.1-400">JVMS 4.7.9.1-400</a>
 */
class AsmClassSignatureVisitor extends SignatureVisitor {
    private static Logger logger = getLogger(AsmClassSignatureVisitor.class);

    record TypeParameter(String name, List<AsmTypeSignatureVisitor> classBound,
                         List<AsmTypeSignatureVisitor> interfaceBounds) {
        TypeParameter(String name) {
            this(name, new ArrayList<>(), new ArrayList<>());
        }
    }

    private final List<TypeParameter> typeParameters = new ArrayList<>();
    private transient TypeParameter currentTypeParameter;

    private AsmTypeSignatureVisitor superclassAsmTypeSignatureVisitor;
    private final List<AsmTypeSignatureVisitor> interfaceAsmTypeSignatureVisitors = new ArrayList<>();

    protected AsmClassSignatureVisitor(int api) {
        super(api);
    }

    @Override
    public void visitFormalTypeParameter(String name) {
        logger.debug("visitFormalTypeParameter:{}", name);
        typeParameters.add(currentTypeParameter = new TypeParameter(name));
    }

    @Override
    public SignatureVisitor visitClassBound() {
        logger.debug("visitClassBound");
        AsmTypeSignatureVisitor visitor = new AsmTypeSignatureVisitor(this.api);
        List<AsmTypeSignatureVisitor> list = currentTypeParameter.classBound();
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
        currentTypeParameter.interfaceBounds().add(visitor);
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

    public String simpleText() {
        return "extends [%s] implements %s".formatted(
                superclassAsmTypeSignatureVisitor.generateParameterizedType().asSimpleText(),
                interfaceAsmTypeSignatureVisitors.stream()
                        .map(AsmTypeSignatureVisitor::generateParameterizedType)
                        .map(ParameterizedType::asSimpleText)
                        .toList());
    }

    ParameterizedType superclass() {
        return superclassAsmTypeSignatureVisitor.generateParameterizedType();
    }

    List<ParameterizedType> interfaces() {
        return interfaceAsmTypeSignatureVisitors.stream()
                .map(AsmTypeSignatureVisitor::generateParameterizedType)
                .toList();
    }

    public List<String> typeParameterNames() {
        return typeParameters.stream().map(TypeParameter::name).toList();
    }
}
