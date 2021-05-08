package org.dddjava.jig.infrastructure.javaparser;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.dddjava.jig.domain.model.parts.classes.method.Arguments;
import org.dddjava.jig.domain.model.parts.classes.method.MethodComment;
import org.dddjava.jig.domain.model.parts.classes.method.MethodIdentifier;
import org.dddjava.jig.domain.model.parts.classes.method.MethodSignature;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.parts.comment.Comment;

import java.util.Collections;
import java.util.List;

class MethodVisitor extends VoidVisitorAdapter<List<MethodComment>> {
    private final TypeIdentifier typeIdentifier;

    public MethodVisitor(TypeIdentifier typeIdentifier) {
        this.typeIdentifier = typeIdentifier;
    }

    @Override
    public void visit(MethodDeclaration n, List<MethodComment> methodComments) {
        n.getJavadoc().ifPresent(javadoc -> {
            String javadocText = javadoc.getDescription().toText();

            MethodComment methodComment = new MethodComment(
                    new MethodIdentifier(
                            typeIdentifier,
                            new MethodSignature(
                                    n.getNameAsString(),
                                    // TODO 引数を取得したい
                                    // n.getParameters() でパラメタは取れるが、fqnは直接とれず、もしとれたとしても確実ではない。
                                    // Argumentとして候補を取り扱ってマッチさせる、といったのがあればいい？それともbyteCode由来のMethodをこのタイミングで探す？
                                    new Arguments(Collections.emptyList())
                            )),
                    Comment.fromCodeComment(javadocText)
            );
            methodComments.add(methodComment);
        });
    }
}
