package org.dddjava.jig.infrastructure.javaparser;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.RecordDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithJavadoc;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.ast.stmt.LocalClassDeclarationStmt;
import com.github.javaparser.ast.stmt.LocalRecordDeclarationStmt;
import com.github.javaparser.ast.visitor.Visitable;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.dddjava.jig.domain.model.models.domains.categories.enums.EnumConstant;
import org.dddjava.jig.domain.model.models.domains.categories.enums.EnumModel;
import org.dddjava.jig.domain.model.parts.classes.method.MethodImplementation;
import org.dddjava.jig.domain.model.parts.classes.type.ClassComment;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.parts.comment.Comment;
import org.dddjava.jig.domain.model.sources.jigfactory.TextSourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class ClassVisitor extends VoidVisitorAdapter<Void> {
    static Logger logger = LoggerFactory.getLogger(ClassVisitor.class);

    private final String packageName;
    ClassComment classComment;
    List<MethodImplementation> methods = new ArrayList<>();
    EnumModel enumModel;
    TypeIdentifier typeIdentifier;

    public ClassVisitor(String packageName) {
        this.packageName = packageName;
    }

    @Override
    public void visit(ClassOrInterfaceDeclaration node, Void arg) {
        visitTopNode(node);
        // クラスの中を読む必要が出てきたらこのコメントを外す
        // super.visit(node, arg);
    }

    @Override
    public void visit(EnumDeclaration node, Void arg) {
        TypeIdentifier typeIdentifier = visitTopNode(node);

        List<EnumConstant> constants = node.getEntries().stream()
                .map(d -> new EnumConstant(d.getNameAsString(), d.getArguments().stream().map(expr -> expr.toString()).collect(Collectors.toList())))
                .collect(Collectors.toList());
        enumModel = new EnumModel(typeIdentifier, constants);
        super.visit(node, arg);
    }

    @Override
    public void visit(ConstructorDeclaration n, Void arg) {
        // enumの時だけコンストラクタの引数名を取る
        if (enumModel != null) {
            enumModel.addConstructorArgumentNames(n.getParameters().stream().map(e -> e.getName().asString()).collect(Collectors.toList()));
        }
        super.visit(n, arg);
    }

    @Override
    public void visit(RecordDeclaration n, Void arg) {
        visitTopNode(n);
        // recordの中を読む必要がある場合
        // super.visit(n, arg);
    }

    @Override
    public void visit(LocalRecordDeclarationStmt n, Void arg) {
        // メソッド内のRecordに対応する必要がある場合
        super.visit(n, arg);
    }

    @Override
    public void visit(LocalClassDeclarationStmt n, Void arg) {
        // メソッド内のclassに対応する必要がある場合
        super.visit(n, arg);
    }

    private <T extends NodeWithSimpleName<?> & NodeWithJavadoc<?> & Visitable> TypeIdentifier visitTopNode(T node) {
        if (typeIdentifier != null) {
            logger.warn("1つの *.java ファイルの2つ目以降の class/interface/enum には現在対応していません。対応が必要な場合は読ませたい構造のサンプルを添えてIssueを作成してください。");
            return typeIdentifier;
        }
        typeIdentifier = new TypeIdentifier(packageName + node.getNameAsString());
        // クラスのJavadocが記述されていれば採用
        node.getJavadoc().ifPresent(javadoc -> {
            String javadocText = javadoc.getDescription().toText();
            classComment = new ClassComment(typeIdentifier, Comment.fromCodeComment(javadocText));
        });
        node.accept(new MethodVisitor(typeIdentifier), methods);

        return typeIdentifier;
    }

    public TextSourceModel toTextSourceModel() {
        return new TextSourceModel(
                classComment != null ? List.of(classComment) : List.of(),
                methods,
                enumModel != null ? List.of(enumModel) : List.of());
    }
}
