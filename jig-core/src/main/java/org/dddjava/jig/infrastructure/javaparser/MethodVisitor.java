package org.dddjava.jig.infrastructure.javaparser;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.dddjava.jig.domain.model.data.classes.method.*;
import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.data.comment.Comment;

import java.util.Collections;
import java.util.List;

class MethodVisitor extends VoidVisitorAdapter<List<MethodImplementation>> {
    private final TypeIdentifier typeIdentifier;

    public MethodVisitor(TypeIdentifier typeIdentifier) {
        this.typeIdentifier = typeIdentifier;
    }

    @Override
    public void visit(MethodDeclaration n, List<MethodImplementation> collector) {
        MethodIdentifier methodIdentifier = new MethodIdentifier(
                typeIdentifier,
                new MethodSignature(
                        n.getNameAsString(),
                        // TODO 引数を取得したい
                        // n.getParameters() でパラメタは取れるが、fqnは直接とれず、もしとれたとしても確実ではない。
                        // Argumentとして候補を取り扱ってマッチさせる、といったのがあればいい？それともbyteCode由来のMethodをこのタイミングで探す？
                        new Arguments(Collections.emptyList())
                ));

        collector.add(
                n.getJavadoc().map(javadoc ->
                        new MethodImplementation(methodIdentifier,
                                new MethodComment(methodIdentifier, Comment.fromCodeComment(javadoc.getDescription().toText()))
                        )).orElseGet(() -> new MethodImplementation(methodIdentifier))
        );
    }
}
