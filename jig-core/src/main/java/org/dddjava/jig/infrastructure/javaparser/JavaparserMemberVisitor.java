package org.dddjava.jig.infrastructure.javaparser;

import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.dddjava.jig.application.GlossaryRepository;
import org.dddjava.jig.domain.model.data.members.fields.JigFieldId;
import org.dddjava.jig.domain.model.data.members.methods.JavaMethodDeclarator;
import org.dddjava.jig.domain.model.data.types.TypeId;

/**
 * メソッドやフィールドからの情報の読み取り
 *
 * 1つのVisitorで色々やると、構造によって予期しない読み方をすることがあるので分けている。
 * 現在はコメントしか読んでいないが、実装からしか取得できない情報を扱う場合はここで扱う。
 */
class JavaparserMemberVisitor extends VoidVisitorAdapter<GlossaryRepository> {
    private final TypeId typeId;

    public JavaparserMemberVisitor(TypeId typeId) {
        this.typeId = typeId;
    }

    @Override
    public void visit(FieldDeclaration n, GlossaryRepository glossaryRepository) {
        n.getJavadoc().ifPresent(javadoc -> {
            // フィールドは複数の変数を宣言できるのでVariablesで処理する必要がある
            // variableごとにコメントは書けるが、宣言についているものを採用する
            var variables = n.getVariables();
            variables.forEach(v -> {
                glossaryRepository.register(
                        TermFactory.fromField(
                                glossaryRepository.fromFieldIdentifier(JigFieldId.from(typeId, v.getNameAsString())),
                                javadoc.getDescription().toText()
                        ));
            });
        });
    }

    @Override
    public void visit(MethodDeclaration n, GlossaryRepository glossaryRepository) {
        var methodImplementationDeclarator = new JavaMethodDeclarator(
                typeId,
                n.getNameAsString(),
                n.getParameters().stream()
                        .map(parameter -> {
                            var type = parameter.getType();
                            if (type.isClassOrInterfaceType()) {
                                return type.asClassOrInterfaceType().getNameAsString();
                            } else {
                                return type.asString();
                            }
                        })
                        .toList()
        );

        n.getJavadoc().ifPresent(javadoc ->
                glossaryRepository.register(TermFactory.fromMethod(glossaryRepository.fromMethodImplementationDeclarator(typeId, methodImplementationDeclarator), methodImplementationDeclarator, javadoc.getDescription().toText()))
        );
    }
}
