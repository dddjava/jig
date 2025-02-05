package org.dddjava.jig.infrastructure.javaparser;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.dddjava.jig.domain.model.data.classes.method.JavaMethodDeclarator;
import org.dddjava.jig.domain.model.data.term.Term;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

import java.util.function.Consumer;

class JavaparserMethodVisitor extends VoidVisitorAdapter<Consumer<Term>> {
    private final TypeIdentifier typeIdentifier;

    public JavaparserMethodVisitor(TypeIdentifier typeIdentifier) {
        this.typeIdentifier = typeIdentifier;
    }

    @Override
    public void visit(MethodDeclaration n, Consumer<Term> termCollector) {
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
                termCollector.accept(Term.fromMethod(typeIdentifier, methodImplementationDeclarator, javadoc.getDescription().toText()))
        );
    }
}
