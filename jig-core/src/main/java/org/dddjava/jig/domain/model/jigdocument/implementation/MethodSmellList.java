package org.dddjava.jig.domain.model.jigdocument.implementation;

import org.dddjava.jig.domain.model.jigmodel.businessrules.BusinessRule;
import org.dddjava.jig.domain.model.jigmodel.businessrules.BusinessRules;
import org.dddjava.jig.domain.model.jigmodel.jigtype.member.JigMethod;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.relation.method.MethodRelations;

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
        for (BusinessRule businessRule : businessRules.list()) {
            for (JigMethod method : businessRule.instanceMethods().list()) {
                MethodSmell methodSmell = new MethodSmell(
                        method,
                        businessRule.fields().fieldDeclarations(),
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
}
