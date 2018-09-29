package org.dddjava.jig.domain.model.smells;

import org.dddjava.jig.domain.model.characteristic.Characteristic;
import org.dddjava.jig.domain.model.characteristic.CharacterizedTypes;
import org.dddjava.jig.domain.model.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.implementation.bytecode.MethodRelations;
import org.dddjava.jig.domain.model.implementation.bytecode.MethodUsingFields;
import org.dddjava.jig.domain.model.unit.method.Method;
import org.dddjava.jig.domain.model.unit.method.Methods;

import java.util.ArrayList;
import java.util.List;

/**
 * メソッドの不吉なにおい一覧
 */
public class MethodSmellAngles {
    List<MethodSmellAngle> list;

    public MethodSmellAngles(Methods methods, CharacterizedTypes characterizedTypes, MethodUsingFields methodUsingFields, FieldDeclarations fieldDeclarations, MethodRelations methodRelations) {
        this.list = new ArrayList<>();
        for (Method method : methods.list()) {
            if (characterizedTypes.stream().pickup(method.declaration().declaringType()).has(Characteristic.MODEL)) {
                MethodSmellAngle methodSmellAngle = new MethodSmellAngle(
                        method,
                        methodUsingFields,
                        fieldDeclarations.filterDeclareTypeIs(method.declaration().declaringType()));
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
