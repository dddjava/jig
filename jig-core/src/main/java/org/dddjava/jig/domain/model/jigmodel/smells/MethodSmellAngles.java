package org.dddjava.jig.domain.model.jigmodel.smells;

import org.dddjava.jig.domain.model.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.jigloaded.relation.method.MethodRelations;
import org.dddjava.jig.domain.model.jigloaded.richmethod.Method;
import org.dddjava.jig.domain.model.jigloaded.richmethod.Methods;
import org.dddjava.jig.domain.model.jigmodel.businessrules.BusinessRules;

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
