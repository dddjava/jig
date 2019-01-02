package org.dddjava.jig.domain.model.smells;

import org.dddjava.jig.domain.model.businessrules.BusinessRules;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.implementation.analyzed.networks.method.MethodRelations;
import org.dddjava.jig.domain.model.implementation.analyzed.unit.method.Method;
import org.dddjava.jig.domain.model.implementation.analyzed.unit.method.Methods;

import java.util.ArrayList;
import java.util.List;

/**
 * メソッドの不吉なにおい一覧
 */
public class MethodSmellAngles {
    List<MethodSmellAngle> list;

    public MethodSmellAngles(Methods methods, FieldDeclarations fieldDeclarations, MethodRelations methodRelations, BusinessRules businessRules) {
        this.list = new ArrayList<>();
        for (Method method : methods.list()) {
            if (businessRules.contains(method.declaration().declaringType())) {
                MethodSmellAngle methodSmellAngle = new MethodSmellAngle(
                        method,
                        fieldDeclarations.filterDeclareTypeIs(method.declaration().declaringType()),
                        methodRelations
                );
                if (methodSmellAngle.hasSmell()) {
                    list.add(methodSmellAngle);
                }
            }
        }
    }

    public List<MethodSmellAngle> list() {
        return list;
    }
}
