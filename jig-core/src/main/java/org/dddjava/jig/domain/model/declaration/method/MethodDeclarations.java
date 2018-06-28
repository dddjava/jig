package org.dddjava.jig.domain.model.declaration.method;

import org.dddjava.jig.domain.basic.Text;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifiers;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * メソッド定義一覧
 */
public class MethodDeclarations {

    List<MethodDeclaration> list;

    public MethodDeclarations(List<MethodDeclaration> list) {
        this.list = list;
    }

    public List<MethodDeclaration> list() {
        return list.stream().sorted(Comparator.comparing(MethodDeclaration::asFullNameText)).collect(Collectors.toList());
    }

    public static Collector<MethodDeclaration, ?, MethodDeclarations> collector() {
        return Collectors.collectingAndThen(toList(), MethodDeclarations::new);
    }

    public String asSimpleText() {
        return list.stream()
                .map(MethodDeclaration::asSimpleTextWithDeclaringType)
                .sorted()
                .collect(Text.collectionCollector());
    }

    public boolean contains(MethodDeclaration methodDeclaration) {
        return list.stream().anyMatch(methodDeclaration::sameIdentifier);
    }

    public TypeIdentifiers declaringTypes() {
        return new TypeIdentifiers(
                list.stream()
                        .map(MethodDeclaration::declaringType)
                        .sorted(Comparator.comparing(TypeIdentifier::fullQualifiedName))
                        .distinct()
                        .collect(toList())
        );
    }

    public boolean empty() {
        return list.isEmpty();
    }

    public MethodDeclarations filterDeclareTypeIs(TypeIdentifier typeIdentifier) {
        return list.stream()
                .filter(methodDeclaration -> methodDeclaration.declaringType().equals(typeIdentifier))
                .collect(collector());
    }

    public MethodNumber number() {
        return new MethodNumber(list.size());
    }

    public String asSignatureAndReturnTypeSimpleText() {
        return list.stream()
                .map(MethodDeclaration::asSignatureAndReturnTypeSimpleText)
                .sorted()
                .collect(Text.collectionCollector());
    }
}
