package org.dddjava.jig.infrastructure.javaparser;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.RecordDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithJavadoc;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.ast.stmt.LocalClassDeclarationStmt;
import com.github.javaparser.ast.stmt.LocalRecordDeclarationStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.dddjava.jig.domain.model.data.classes.method.MethodImplementation;
import org.dddjava.jig.domain.model.data.classes.type.ClassComment;
import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.data.comment.Comment;
import org.dddjava.jig.domain.model.data.enums.EnumConstant;
import org.dddjava.jig.domain.model.data.enums.EnumModel;
import org.dddjava.jig.domain.model.sources.jigfactory.TextSourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class JavaparserClassVisitor extends VoidVisitorAdapter<AdditionalSourceModelBuilder> {
    static Logger logger = LoggerFactory.getLogger(JavaparserClassVisitor.class);

    private final String packageName;
    ClassComment classComment;
    List<MethodImplementation> methods = new ArrayList<>();
    EnumModel enumModel;
    TypeIdentifier typeIdentifier;

    public JavaparserClassVisitor(String packageName) {
        this.packageName = packageName;
    }

    @Override
    public void visit(PackageDeclaration packageDeclaration, AdditionalSourceModelBuilder arg) {
        arg.setPackage(packageDeclaration);
    }

    @Override
    public void visit(ImportDeclaration importDeclaration, AdditionalSourceModelBuilder arg) {
        arg.addImport(importDeclaration);
    }

    @Override
    public void visit(ClassOrInterfaceDeclaration node, AdditionalSourceModelBuilder arg) {
        visitClassOrInterfaceOrEnumOrRecord(node, arg);
    }

    @Override
    public void visit(EnumDeclaration node, AdditionalSourceModelBuilder arg) {
        TypeIdentifier typeIdentifier = visitClassOrInterfaceOrEnumOrRecord(node, arg);

        List<EnumConstant> constants = node.getEntries().stream()
                .map(d -> new EnumConstant(d.getNameAsString(), d.getArguments().stream().map(expr -> expr.toString()).collect(Collectors.toList())))
                .collect(Collectors.toList());
        enumModel = new EnumModel(typeIdentifier, constants);
        super.visit(node, arg);
    }

    @Override
    public void visit(ConstructorDeclaration n, AdditionalSourceModelBuilder arg) {
        // enumの時だけコンストラクタの引数名を取る
        if (enumModel != null) {
            enumModel.addConstructorArgumentNames(n.getParameters().stream().map(e -> e.getName().asString()).collect(Collectors.toList()));
        }
        super.visit(n, arg);
    }

    @Override
    public void visit(RecordDeclaration n, AdditionalSourceModelBuilder arg) {
        visitClassOrInterfaceOrEnumOrRecord(n, arg);
    }

    @Override
    public void visit(LocalRecordDeclarationStmt n, AdditionalSourceModelBuilder arg) {
        // メソッド内のRecordに対応する必要がある場合
        super.visit(n, arg);
    }

    @Override
    public void visit(LocalClassDeclarationStmt n, AdditionalSourceModelBuilder arg) {
        // メソッド内のclassに対応する必要がある場合
        super.visit(n, arg);
    }

    private <T extends Node & NodeWithSimpleName<?> & NodeWithJavadoc<?>> TypeIdentifier visitClassOrInterfaceOrEnumOrRecord(T node, AdditionalSourceModelBuilder arg) {
        if (typeIdentifier != null) {
            logger.warn("1つの *.java ファイルの2つ目以降の class/interface/enum には現在対応していません。対応が必要な場合は読ませたい構造のサンプルを添えてIssueを作成してください。");
            return typeIdentifier;
        }
        arg.setTypeName(node.getNameAsString());

        typeIdentifier = TypeIdentifier.valueOf(packageName + node.getNameAsString());
        // クラスのJavadocが記述されていれば採用
        node.getJavadoc().ifPresent(javadoc -> {
            String javadocText = javadoc.getDescription().toText();
            classComment = new ClassComment(typeIdentifier, Comment.fromCodeComment(javadocText));
        });
        node.accept(new JavaparserMethodVisitor(typeIdentifier), methods);

        return typeIdentifier;
    }

    public TextSourceModel toTextSourceModel() {
        return new TextSourceModel(
                classComment != null ? List.of(classComment) : List.of(),
                methods,
                enumModel != null ? List.of(enumModel) : List.of());
    }
}
