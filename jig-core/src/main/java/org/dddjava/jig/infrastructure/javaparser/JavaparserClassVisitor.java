package org.dddjava.jig.infrastructure.javaparser;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.RecordDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithJavadoc;
import com.github.javaparser.ast.nodeTypes.NodeWithMembers;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.ast.stmt.LocalClassDeclarationStmt;
import com.github.javaparser.ast.stmt.LocalRecordDeclarationStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.dddjava.jig.application.GlossaryRepository;
import org.dddjava.jig.domain.model.data.enums.EnumConstant;
import org.dddjava.jig.domain.model.data.enums.EnumModel;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.sources.javasources.JavaSourceModel;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * クラスからの情報の読み取り
 *
 * 主な役割はコメントを読むことだが、enumの情報はクラスファイルには残っていないのでここで取得している。
 */
class JavaparserClassVisitor extends VoidVisitorAdapter<GlossaryRepository> {
    static Logger logger = LoggerFactory.getLogger(JavaparserClassVisitor.class);

    private final String packageName;
    @Nullable
    private TypeId typeId;

    private Optional<EnumModel> enumModel = Optional.empty();

    public JavaparserClassVisitor(String packageName) {
        this.packageName = packageName;
    }

    @Override
    public void visit(PackageDeclaration packageDeclaration, GlossaryRepository arg) {
    }

    @Override
    public void visit(ImportDeclaration importDeclaration, GlossaryRepository arg) {
    }

    @Override
    public void visit(ClassOrInterfaceDeclaration node, GlossaryRepository arg) {
        visitClassOrInterfaceOrEnumOrRecord(node, arg);
    }

    @Override
    public void visit(EnumDeclaration node, GlossaryRepository arg) {
        TypeId typeId = visitClassOrInterfaceOrEnumOrRecord(node, arg);

        List<EnumConstant> constants = node.getEntries().stream()
                .map(d -> new EnumConstant(d.getNameAsString(), d.getArguments().stream().map(expr -> expr.toString()).toList()))
                .toList();
        enumModel = Optional.of(new EnumModel(typeId, constants));
        super.visit(node, arg);
    }

    @Override
    public void visit(ConstructorDeclaration n, GlossaryRepository arg) {
        // enumの時だけコンストラクタの引数名を取る
        enumModel.ifPresent(it -> it.addConstructorArgumentNames(n.getParameters().stream().map(e -> e.getName().asString()).toList()));
        super.visit(n, arg);
    }

    @Override
    public void visit(RecordDeclaration n, GlossaryRepository arg) {
        visitClassOrInterfaceOrEnumOrRecord(n, arg);
    }

    @Override
    public void visit(LocalRecordDeclarationStmt n, GlossaryRepository arg) {
        // メソッド内のRecordに対応する必要がある場合
        super.visit(n, arg);
    }

    @Override
    public void visit(LocalClassDeclarationStmt n, GlossaryRepository arg) {
        // メソッド内のclassに対応する必要がある場合
        super.visit(n, arg);
    }

    private <T extends Node & NodeWithSimpleName<?> & NodeWithJavadoc<?> & NodeWithMembers<?>> TypeId visitClassOrInterfaceOrEnumOrRecord(T node, GlossaryRepository glossaryRepository) {
        var fqn = packageName + node.getNameAsString();

        if (typeId != null) {
            logger.warn("1つの *.java ファイルの2つ目以降の class/interface/enum/record には対応していません。{} のロードはスキップされます。対応が必要な場合は読ませたい構造のサンプルを添えてIssueを作成してください。",
                    fqn
            );
            return typeId;
        }

        typeId = TypeId.valueOf(fqn);
        // クラスのJavadocが記述されていれば採用
        node.getJavadoc().ifPresent(javadoc -> {
            String javadocText = javadoc.getDescription().toText();
            glossaryRepository.register(TermFactory.fromClass(glossaryRepository.fromTypeId(typeId), javadocText));
        });
        node.accept(new JavaparserMemberVisitor(typeId), glossaryRepository);

        node.getMembers().forEach(member -> {
            if (member instanceof ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
                logger.debug("nested class or interface: {}", classOrInterfaceDeclaration.getFullyQualifiedName());
            }
        });

        return typeId;
    }

    public JavaSourceModel javaSourceModel() {
        return JavaSourceModel.from(enumModel.map(List::of).orElseGet(List::of));
    }
}
