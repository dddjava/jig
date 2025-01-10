package org.dddjava.jig.domain.model.knowledge.smell;

import org.dddjava.jig.domain.model.data.classes.method.MethodRelations;
import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.information.domains.businessrules.BusinessRules;
import org.dddjava.jig.domain.model.information.jigobject.class_.JigType;
import org.dddjava.jig.domain.model.information.jigobject.member.JigMethod;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * メソッドの不吉なにおい一覧
 */
public class MethodSmellList {
    List<MethodSmell> list;

    public MethodSmellList(BusinessRules businessRules, MethodRelations methodRelations) {
        this.list = new ArrayList<>();
        for (JigType jigType : businessRules.list()) {
            for (JigMethod method : jigType.instanceMember().instanceMethods().list()) {
                MethodSmell methodSmell = new MethodSmell(
                        method,
                        jigType.instanceMember().fieldDeclarations(),
                        methodRelations
                );
                if (methodSmell.hasSmell()) {
                    list.add(methodSmell);
                }
            }
        }
    }

    public List<MethodSmell> list() {
        return list.stream()
                .sorted(Comparator.comparing(methodSmell -> methodSmell.methodDeclaration().asFullNameText()))
                .collect(Collectors.toList());
    }

    public List<MethodSmell> collectBy(TypeIdentifier typeIdentifier) {
        return list.stream()
                .filter(methodSmell -> methodSmell.methodDeclaration().declaringType().equals(typeIdentifier))
                .toList();
    }
}
