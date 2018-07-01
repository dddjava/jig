package org.dddjava.jig.domain.model.booleans.model;

import org.dddjava.jig.domain.model.characteristic.CharacterizedMethods;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.implementation.bytecode.MethodRelations;

import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * 真偽値を返すモデルの切り口一覧
 */
public class BoolQueryAngles {
    List<BoolQueryAngle> values;

    public BoolQueryAngles(List<BoolQueryAngle> values) {
        this.values = values;
    }

    public static BoolQueryAngles of(CharacterizedMethods methods, MethodRelations relations) {
        MethodDeclarations modelBoolQueryMethods = methods.modelBoolQueryMethods();
        List<BoolQueryAngle> list = modelBoolQueryMethods.list().stream()
                .map(method -> new BoolQueryAngle(
                        method,
                        relations.userMethodsOf(method)
                )).collect(toList());
        return new BoolQueryAngles(list);
    }

    public List<BoolQueryAngle> list() {
        return values;
    }
}
