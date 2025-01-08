package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.data.classes.method.Arguments;
import org.dddjava.jig.domain.model.data.classes.method.MethodDeclaration;
import org.dddjava.jig.domain.model.data.classes.method.MethodReturn;
import org.dddjava.jig.domain.model.data.classes.method.MethodSignature;
import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;
import org.objectweb.asm.signature.SignatureVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MethodSignature =
 * ( visitFormalTypeParameter visitClassBound? visitInterfaceBound* )*
 * (visitParameterType* visitReturnType visitExceptionType* )
 */
class MethodSignatureVisitor extends SignatureVisitor {
    List<TypeSignatureVisitor> parameterVisitors;
    TypeSignatureVisitor returnVisitor;

    public MethodSignatureVisitor(int api) {
        super(api);
        parameterVisitors = new ArrayList<>();
        returnVisitor = new TypeSignatureVisitor(this.api);
    }

    @Override
    public SignatureVisitor visitParameterType() {
        TypeSignatureVisitor visitor = new TypeSignatureVisitor(this.api);
        parameterVisitors.add(visitor);
        return visitor;
    }

    @Override
    public SignatureVisitor visitReturnType() {
        return returnVisitor;
    }

    MethodDeclaration methodDeclaration(TypeIdentifier declaringType, String methodName) {
        return new MethodDeclaration(
                declaringType,
                new MethodSignature(
                        methodName,
                        Arguments.from(parameterVisitors.stream()
                                .map(parameterVisitor -> parameterVisitor.generateParameterizedType())
                                .collect(Collectors.toList()))
                ),
                new MethodReturn(returnVisitor.generateParameterizedType())
        );
    }
}