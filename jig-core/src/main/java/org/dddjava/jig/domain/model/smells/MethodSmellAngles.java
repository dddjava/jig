package org.dddjava.jig.domain.model.smells;

import org.dddjava.jig.domain.model.characteristic.Characteristic;
import org.dddjava.jig.domain.model.characteristic.CharacterizedTypes;
import org.dddjava.jig.domain.model.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.declaration.method.Methods;
import org.dddjava.jig.domain.model.implementation.bytecode.MethodUsingFields;

import java.util.List;
import java.util.stream.Collectors;

/**
 * メソッドの不吉なにおい一覧
 */
public class MethodSmellAngles {
    List<MethodSmellAngle> list;

    public MethodSmellAngles(Methods methods, CharacterizedTypes characterizedTypes, MethodUsingFields methodUsingFields, FieldDeclarations fieldDeclarations) {
        this.list = methods.list().stream()
                .filter(method -> characterizedTypes.stream().pickup(method.declaration().declaringType()).has(Characteristic.MODEL))
                .map(method -> new MethodSmellAngle(method, methodUsingFields, fieldDeclarations.filterDeclareTypeIs(method.declaration().declaringType())))
                .filter(MethodSmellAngle::hasSmell)
                .collect(Collectors.toList());
    }

    public List<MethodSmellAngle> list() {
        return list;
    }
}
