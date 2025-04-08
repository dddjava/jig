package org.dddjava.jig.infrastructure.javaparser;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.dddjava.jig.application.GlossaryRepository;
import org.dddjava.jig.domain.model.data.members.methods.JavaMethodDeclarator;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

/**
 * メソッドからの情報の読み取り
 *
 * 1つのVisitorで色々やると、構造によって予期しない読み方をすることがあるので分けている。
 * 現在はコメントしか読んでいないが、実装からしか取得できない情報を扱う場合はここで扱う。
 */
class JavaparserMethodVisitor extends VoidVisitorAdapter<GlossaryRepository> {
    private final TypeIdentifier typeIdentifier;

    public JavaparserMethodVisitor(TypeIdentifier typeIdentifier) {
        this.typeIdentifier = typeIdentifier;
    }

    @Override
    public void visit(MethodDeclaration n, GlossaryRepository glossaryRepository) {
        var methodImplementationDeclarator = new JavaMethodDeclarator(
                typeIdentifier,
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
                glossaryRepository.register(TermFactory.fromMethod(glossaryRepository.fromMethodImplementationDeclarator(typeIdentifier, methodImplementationDeclarator), methodImplementationDeclarator, javadoc.getDescription().toText()))
        );
    }
}
