package org.dddjava.jig.infrastructure.javaparser;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.dddjava.jig.application.GlossaryRepository;
import org.dddjava.jig.domain.model.data.classes.method.JavaMethodDeclarator;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

class JavaparserMethodVisitor extends VoidVisitorAdapter<GlossaryRepository> {
    private final TypeIdentifier typeIdentifier;

    public JavaparserMethodVisitor(TypeIdentifier typeIdentifier) {
        this.typeIdentifier = typeIdentifier;
    }

    @Override
    public void visit(MethodDeclaration n, GlossaryRepository glossaryRepository) {
        var methodImplementationDeclarator = new JavaMethodDeclarator(
                typeIdentifier,
                n.getNameAsString(),
                n.getParameters().stream()
                        .map(parameter -> {
                            var type = parameter.getType();
                            if (type.isClassOrInterfaceType()) {
                                return type.asClassOrInterfaceType().getNameAsString();
                            } else {
                                return type.asString();
                            }
                        })
                        .toList()
        );

        n.getJavadoc().ifPresent(javadoc ->
                glossaryRepository.register(TermFactory.fromMethod(glossaryRepository.fromMethodImplementationDeclarator(typeIdentifier, methodImplementationDeclarator), methodImplementationDeclarator, javadoc.getDescription().toText()))
        );
    }
}
