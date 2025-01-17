package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.data.classes.method.MethodDeclaration;
import org.dddjava.jig.domain.model.data.classes.method.MethodReturn;
import org.dddjava.jig.domain.model.data.classes.method.MethodSignature;
import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * MethodSignature =
 * ( visitFormalTypeParameter visitClassBound? visitInterfaceBound* )*
 * (visitParameterType* visitReturnType visitExceptionType* )
 */
class AsmMethodSignatureVisitor extends SignatureVisitor {
    private static final Logger logger = LoggerFactory.getLogger(AsmMethodSignatureVisitor.class);
    List<AsmTypeSignatureVisitor> parameterVisitors;
    AsmTypeSignatureVisitor returnVisitor;

    public AsmMethodSignatureVisitor(int api) {
        super(api);
        parameterVisitors = new ArrayList<>();
        returnVisitor = new AsmTypeSignatureVisitor(this.api);
    }

    /**
     * 型の仮引数名。ジェネリクスメソッドで登場。バインドした場合は出てこない。
     */
    @Override
    public void visitFormalTypeParameter(String name) {
        logger.debug("visitFormalTypeParameter:{}", name);
        super.visitFormalTypeParameter(name);
    }

    @Override
    public SignatureVisitor visitClassBound() {
        logger.debug("visitClassBound");
        return super.visitClassBound();
    }

    @Override
    public SignatureVisitor visitInterfaceBound() {
        logger.debug("visitInterfaceBound");
        return super.visitInterfaceBound();
    }

    @Override
    public SignatureVisitor visitParameterType() {
        logger.debug("visitParameterType");
        AsmTypeSignatureVisitor visitor = new AsmTypeSignatureVisitor(this.api);
        parameterVisitors.add(visitor);
        return visitor;
    }

    @Override
    public SignatureVisitor visitReturnType() {
        logger.debug("visitReturnType");
        return returnVisitor;
    }

    @Override
    public SignatureVisitor visitExceptionType() {
        logger.debug("visitExceptionType");
        return super.visitExceptionType();
    }

    public MethodDeclaration buildMethodDeclaration(TypeIdentifier declaringType, String methodName) {
        return new MethodDeclaration(
                declaringType,
                MethodSignature.from(
                        methodName,
                        parameterVisitors.stream()
                                .map(parameterVisitor -> parameterVisitor.generateParameterizedType())
                                .collect(Collectors.toList())
                ),
                new MethodReturn(returnVisitor.generateParameterizedType())
        );
    }

    static Optional<MethodDeclaration> buildMethodDeclaration(int api, String name, TypeIdentifier declaringType, String signature) {
        try {
            AsmMethodSignatureVisitor methodSignatureVisitor = new AsmMethodSignatureVisitor(api);
            new SignatureReader(signature).accept(methodSignatureVisitor);
            return Optional.of(methodSignatureVisitor.buildMethodDeclaration(declaringType, name));
        } catch (Exception e) {
            logger.warn("exception occurred reading method signature {} for {} {}", signature, declaringType, name, e);
            return Optional.empty();
        }
    }
}