package org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.method;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.text.Text;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifiers;

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
        return Text.sortedOf(list, MethodDeclaration::asSimpleTextWithDeclaringType);
    }

    public boolean contains(MethodDeclaration methodDeclaration) {
        return list.stream().anyMatch(methodDeclaration::sameIdentifier);
    }

    public boolean empty() {
        return list.isEmpty();
    }

    public MethodNumber number() {
        return new MethodNumber(list.size());
    }

    public String asSignatureAndReturnTypeSimpleText() {
        return Text.sortedOf(list, MethodDeclaration::asSignatureAndReturnTypeSimpleText);
    }

    public TypeIdentifiers returnTypeIdentifiers() {
        return list.stream()
                .map(MethodDeclaration::methodReturn)
                .map(MethodReturn::typeIdentifier)
                // voidは除く
                .filter(typeIdentifier -> !typeIdentifier.isVoid())
                .collect(TypeIdentifiers.collector());
    }

    public TypeIdentifiers argumentsTypeIdentifiers() {
        return list.stream()
                .map(MethodDeclaration::argumentsTypeIdentifiers)
                .flatMap(List::stream)
                .collect(TypeIdentifiers.collector());
    }
}
