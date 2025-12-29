package org.dddjava.jig.infrastructure.javaparser;

import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.dddjava.jig.application.GlossaryRepository;
import org.dddjava.jig.domain.model.data.enums.EnumConstant;
import org.dddjava.jig.domain.model.data.enums.EnumModel;
import org.dddjava.jig.domain.model.data.members.fields.JigFieldId;
import org.dddjava.jig.domain.model.data.types.TypeId;

import java.util.ArrayList;

/**
 * enum 固有の情報を読み取るVisitor
 *
 * JavaparserClassVisitorから使用する。
 */
class JavaparserEnumVisitor extends VoidVisitorAdapter<GlossaryRepository> {

    private final TypeId typeId;

    /**
     * 列挙定数のリスト
     */
    private final ArrayList<EnumConstant> constantsList = new ArrayList<>();
    private final ArrayList<String> constructorParameterNameList = new ArrayList<>();

    public JavaparserEnumVisitor(TypeId typeId) {
        this.typeId = typeId;
    }

    @Override
    public void visit(EnumDeclaration enumDeclaration, GlossaryRepository arg) {
        super.visit(enumDeclaration, arg);
    }

    /**
     * コンストラクタ
     *
     * コンストラクタが複数ある場合はこれが複数呼ばれる
     */
    @Override
    public void visit(ConstructorDeclaration constructorDeclaration, GlossaryRepository arg) {
        // TODO 複数のコンストラクタがある場合に対応できていない
        constructorParameterNameList.addAll(constructorDeclaration.getParameters().stream()
                .map(e -> e.getName().asString())
                .toList());
        super.visit(constructorDeclaration, arg);
    }

    /**
     * 列挙定数
     * 記述されている順で一つづつ呼ばれる
     */
    @Override
    public void visit(EnumConstantDeclaration enumConstantDeclaration, GlossaryRepository glossaryRepository) {
        var constantName = enumConstantDeclaration.getNameAsString();
        var constantArgumentExpressionList = enumConstantDeclaration.getArguments().stream()
                .map(expr -> {
                    // 記述されているままの文字列で持つ
                    // 文字列表現が合わないもの（たとえば他フィールド参照やラムダ式など）の扱いは改善の余地がある
                    return expr.toString();
                })
                .toList();
        var constant = new EnumConstant(constantName, constantArgumentExpressionList);
        constantsList.add(constant);

        enumConstantDeclaration.getJavadoc().ifPresent(javadoc -> {
            glossaryRepository.register(
                    // 列挙定数は用語集にフィールドとして登録する
                    // バイトコード上はそう表現されるし、Java言語でも列挙定数とフィールドは同じ名前を使用できないので問題ない
                    TermFactory.fromField(
                            glossaryRepository.fromFieldId(JigFieldId.from(typeId, constantName)),
                            javadoc.getDescription().toText()
                    ));
        });
    }

    EnumModel createEnumModel() {
        // TODO EnumModel をイミュータブルにできる
        var enumModel = new EnumModel(typeId, constantsList);
        enumModel.addConstructorArgumentNames(constructorParameterNameList);
        return enumModel;
    }
}
