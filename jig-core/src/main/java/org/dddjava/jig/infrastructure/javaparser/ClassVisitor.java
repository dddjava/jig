package org.dddjava.jig.infrastructure.javaparser;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithJavadoc;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.ast.visitor.Visitable;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.dddjava.jig.domain.model.parts.classes.method.MethodComment;
import org.dddjava.jig.domain.model.parts.classes.type.ClassComment;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.parts.comment.Comment;

import java.util.ArrayList;
import java.util.List;

public class ClassVisitor extends VoidVisitorAdapter<Void> {

    private final String packageName;
    ClassComment classComment = null;
    List<MethodComment> methodComments = new ArrayList<>();

    public ClassVisitor(String packageName) {
        this.packageName = packageName;
    }

    @Override
    public void visit(ClassOrInterfaceDeclaration node, Void arg) {
        visitClassDeclaration(node);
    }

    @Override
    public void visit(EnumDeclaration node, Void arg) {
        visitClassDeclaration(node);
    }

    private <T extends NodeWithSimpleName<?> & NodeWithJavadoc<?> & Visitable> void visitClassDeclaration(T node) {
        TypeIdentifier typeIdentifier = new TypeIdentifier(packageName + node.getNameAsString());
        // クラスのJavadocが記述されていれば採用
        node.getJavadoc().ifPresent(javadoc -> {
            String javadocText = javadoc.getDescription().toText();
            classComment = new ClassComment(typeIdentifier, Comment.fromCodeComment(javadocText));
        });
        node.accept(new MethodVisitor(typeIdentifier), methodComments);
    }

    public TypeSourceResult toTypeSourceResult() {
        return new TypeSourceResult(classComment, methodComments);
    }
}
