package org.dddjava.jig.infrastructure.javaparser;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.dddjava.jig.domain.model.data.classes.method.JavaMethodDeclarator;
import org.dddjava.jig.domain.model.data.classes.method.MethodImplementation;
import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.data.comment.Comment;

import java.util.List;

class JavaparserMethodVisitor extends VoidVisitorAdapter<List<MethodImplementation>> {
    private final TypeIdentifier typeIdentifier;

    public JavaparserMethodVisitor(TypeIdentifier typeIdentifier) {
        this.typeIdentifier = typeIdentifier;
    }

    @Override
    public void visit(MethodDeclaration n, List<MethodImplementation> collector) {
        var methodImplementationDeclarator = new JavaMethodDeclarator(
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

        collector.add(
                n.getJavadoc().map(javadoc ->
                        new MethodImplementation(typeIdentifier, methodImplementationDeclarator,
                                Comment.fromCodeComment(javadoc.getDescription().toText())
                        )).orElseGet(() ->
                        new MethodImplementation(typeIdentifier, methodImplementationDeclarator,
                                Comment.empty()))
        );
    }
}
