package org.dddjava.jig.infrastructure.javaparser;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithJavadoc;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.ast.visitor.Visitable;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.japanese.JapaneseName;
import org.dddjava.jig.domain.model.japanese.MethodJapaneseName;
import org.dddjava.jig.domain.model.japanese.TypeJapaneseName;

import java.util.ArrayList;
import java.util.List;

public class ClassVisitor extends VoidVisitorAdapter<Void> {

    private final String packageName;
    TypeJapaneseName typeJapaneseName = null;
    List<MethodJapaneseName> methodJapaneseNames = new ArrayList<>();

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

    private <T extends NodeWithSimpleName & NodeWithJavadoc<?> & Visitable> void visitClassDeclaration(T node) {
        TypeIdentifier typeIdentifier = new TypeIdentifier(packageName + node.getNameAsString());
        // クラスのJavadocが記述されていれば採用
        node.getJavadoc().ifPresent(javadoc -> {
            String javadocText = javadoc.getDescription().toText();
            JapaneseName japaneseName = new JapaneseName(javadocText);
            typeJapaneseName = new TypeJapaneseName(typeIdentifier, japaneseName);
        });
        node.accept(new MethodVisitor(typeIdentifier), methodJapaneseNames);
    }

    public TypeSourceResult toTypeSourceResult() {
        return new TypeSourceResult(typeJapaneseName, methodJapaneseNames);
    }
}
