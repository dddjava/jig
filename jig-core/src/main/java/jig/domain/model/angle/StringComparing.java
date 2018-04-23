package jig.domain.model.angle;

import jig.domain.model.declaration.method.MethodDeclaration;
import jig.domain.model.declaration.method.MethodDeclarations;
import jig.domain.model.declaration.method.MethodSignature;
import jig.domain.model.identifier.type.TypeIdentifier;
import jig.domain.model.relation.RelationRepository;

import java.util.Collections;

/**
 * 文字列比較を行なっているメソッドを見つける。
 *
 * 文字列比較を行なっているメソッドはビジネスルールの分類判定を行なっている可能性が高い。
 * サービスなどに登場した場合はかなり拙いし、そうでなくても列挙を使用するなど改善の余地がある。
 */
public class StringComparing {

    private final RelationRepository relationRepository;

    public StringComparing(RelationRepository relationRepository) {
        this.relationRepository = relationRepository;
    }

    public MethodDeclarations stringComparingMethods() {
        // String#equals(Object)
        MethodDeclaration equalsMethod = new MethodDeclaration(
                new TypeIdentifier(String.class),
                new MethodSignature(
                        "equals",
                        Collections.singletonList(new TypeIdentifier(Object.class))));

        return relationRepository.findUserMethods(equalsMethod);
    }
}
