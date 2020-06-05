package org.dddjava.jig.domain.model.jigdocument.implementation;

import org.dddjava.jig.domain.model.jigmodel.businessrules.BusinessRules;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.relation.method.MethodRelations;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.richmethod.Method;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.richmethod.Methods;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * メソッドの不吉なにおい一覧
 */
public class MethodSmellList {
    List<MethodSmell> list;

    public MethodSmellList(Methods methods, FieldDeclarations fieldDeclarations, MethodRelations methodRelations, BusinessRules businessRules) {
        this.list = new ArrayList<>();
        for (Method method : methods.list()) {
            if (businessRules.contains(method.declaration().declaringType())) {
                MethodSmell methodSmell = new MethodSmell(
                        method,
                        fieldDeclarations.filterDeclareTypeIs(method.declaration().declaringType()),
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
