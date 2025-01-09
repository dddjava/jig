package org.dddjava.jig.infrastructure.javaparser;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.dddjava.jig.domain.model.data.classes.method.*;
import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.data.comment.Comment;

import java.util.Collections;
import java.util.List;

class JavaparserMethodVisitor extends VoidVisitorAdapter<List<MethodImplementation>> {
    private final TypeIdentifier typeIdentifier;

    public JavaparserMethodVisitor(TypeIdentifier typeIdentifier) {
        this.typeIdentifier = typeIdentifier;
    }

    @Override
    public void visit(MethodDeclaration n, List<MethodImplementation> collector) {
        var methodImplementationDeclarator = new MethodImplementationDeclarator(
                n.getNameAsString(),
                // TODO n.getParameters() から引数型にする。引数名はとりあえずいらない。
                List.of()
        );

        collector.add(
                n.getJavadoc().map(javadoc ->
                        new MethodImplementation(typeIdentifier, methodImplementationDeclarator,
                                new MethodComment(Comment.fromCodeComment(javadoc.getDescription().toText()))
                        )).orElseGet(() ->
                        new MethodImplementation(typeIdentifier, methodImplementationDeclarator,
                                MethodComment.empty()))
        );
    }
}
