package org.dddjava.jig.infrastructure.javaparser;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.dddjava.jig.domain.model.declaration.method.Arguments;
import org.dddjava.jig.domain.model.declaration.method.MethodIdentifier;
import org.dddjava.jig.domain.model.declaration.method.MethodSignature;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.interpret.alias.JavadocAliasSource;
import org.dddjava.jig.domain.model.interpret.alias.MethodAlias;

import java.util.Collections;
import java.util.List;

class MethodVisitor extends VoidVisitorAdapter<List<MethodAlias>> {
    private final TypeIdentifier typeIdentifier;

    public MethodVisitor(TypeIdentifier typeIdentifier) {
        this.typeIdentifier = typeIdentifier;
    }

    @Override
    public void visit(MethodDeclaration n, List<MethodAlias> methodAliases) {
        n.getJavadoc().ifPresent(javadoc -> {
            String javadocText = javadoc.getDescription().toText();

            MethodAlias methodAlias = new MethodAlias(
                    new MethodIdentifier(
                            typeIdentifier,
                            new MethodSignature(
                                    n.getNameAsString(),
                                    // TODO 引数を取得したい
                                    // n.getParameters() でパラメタは取れるが、fqnは直接とれず、もしとれたとしても確実ではない。
                                    // Argumentとして候補を取り扱ってマッチさせる、といったのがあればいい？それともbyteCode由来のMethodをこのタイミングで探す？
                                    new Arguments(Collections.emptyList())
                            )),
                    new JavadocAliasSource(javadocText).toAlias()
            );
            methodAliases.add(methodAlias);
        });
    }
}
