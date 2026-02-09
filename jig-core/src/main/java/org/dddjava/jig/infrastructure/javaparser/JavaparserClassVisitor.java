package org.dddjava.jig.infrastructure.javaparser;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.stmt.LocalClassDeclarationStmt;
import com.github.javaparser.ast.stmt.LocalRecordDeclarationStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.dddjava.jig.application.GlossaryRepository;
import org.dddjava.jig.domain.model.data.enums.EnumModel;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.sources.javasources.JavaSourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * クラスからの情報の読み取り
 *
 * 主な役割はコメントを読むことだが、enumの情報はクラスファイルには残っていないのでここで取得している。
 */
class JavaparserClassVisitor extends VoidVisitorAdapter<GlossaryRepository> {
    private static final Logger logger = LoggerFactory.getLogger(JavaparserClassVisitor.class);

    private final String packageName;
    private final List<EnumModel> enumModels = new ArrayList<>();

    public JavaparserClassVisitor(String packageName) {
        this.packageName = packageName;
    }

    @Override
    public void visit(PackageDeclaration packageDeclaration, GlossaryRepository arg) {
        // package は今のところ使用予定はない
        super.visit(packageDeclaration, arg);
    }

    @Override
    public void visit(ImportDeclaration importDeclaration, GlossaryRepository arg) {
        // import は今のところ使用予定はない
        super.visit(importDeclaration, arg);
    }

    @Override
    public void visit(ClassOrInterfaceDeclaration node, GlossaryRepository arg) {
        visitTypeDeclaration(node, arg);
    }

    @Override
    public void visit(EnumDeclaration enumDeclaration, GlossaryRepository arg) {
        TypeId typeId = visitTypeDeclaration(enumDeclaration, arg);

        // enum　固有の読み取りを行う
        var visitor = new JavaparserEnumVisitor(typeId);
        enumDeclaration.accept(visitor, arg);
        enumModels.add(visitor.createEnumModel());
    }

    @Override
    public void visit(RecordDeclaration recordDeclaration, GlossaryRepository arg) {
        visitTypeDeclaration(recordDeclaration, arg);
    }

    @Override
    public void visit(AnnotationDeclaration annotationDeclaration, GlossaryRepository arg) {
        visitTypeDeclaration(annotationDeclaration, arg);
    }

    @Override
    public void visit(LocalRecordDeclarationStmt localRecordDeclarationStmt, GlossaryRepository arg) {
        // ローカルレコード（メソッド内のRecord）はJIGの関心ある構造でないので対応予定はない
        super.visit(localRecordDeclarationStmt, arg);
    }

    @Override
    public void visit(LocalClassDeclarationStmt localClassDeclarationStmt, GlossaryRepository arg) {
        // ローカルクラス（メソッド内のclass）はJIGの関心ある構造でないので対応予定はない
        super.visit(localClassDeclarationStmt, arg);
    }

    /**
     * 型定義の共通処理
     */
    private TypeId visitTypeDeclaration(TypeDeclaration<?> node, GlossaryRepository glossaryRepository) {
        var typeId = TypeId.valueOf(resolveFqn(node));
        // クラスのJavadocが記述されていれば採用
        node.getJavadoc().ifPresent(javadoc -> {
            String javadocText = javadoc.getDescription().toText();
            glossaryRepository.register(TermFactory.fromClass(glossaryRepository.fromTypeId(typeId), javadocText));
        });
        // メンバの情報を別のVisitorで読む（ネストした型のメンバは対象外）
        var memberVisitor = new JavaparserMemberVisitor(typeId);
        node.getMembers().forEach(member -> {
            if (member instanceof FieldDeclaration || member instanceof MethodDeclaration) {
                member.accept(memberVisitor, glossaryRepository);
            }
            if (member instanceof TypeDeclaration<?> typeDeclaration) {
                typeDeclaration.accept(this, glossaryRepository);
            }
        });

        return typeId;
    }

    private String resolveFqn(TypeDeclaration<?> node) {
        return node.getFullyQualifiedName()
                .orElse(packageName + node.getNameAsString());
    }

    public JavaSourceModel javaSourceModel() {
        return JavaSourceModel.from(enumModels);
    }
}
