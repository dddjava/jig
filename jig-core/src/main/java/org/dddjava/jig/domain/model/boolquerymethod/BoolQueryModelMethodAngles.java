package org.dddjava.jig.domain.model.boolquerymethod;

import org.dddjava.jig.domain.model.characteristic.CharacterizedMethods;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.implementation.relation.MethodRelations;

import static java.util.stream.Collectors.toList;

import java.util.List;

public class BoolQueryModelMethodAngles {
    List<BoolQueryModelMethodAngle> values;

    public BoolQueryModelMethodAngles(List<BoolQueryModelMethodAngle> values) {
        this.values = values;
    }

    public static BoolQueryModelMethodAngles of(CharacterizedMethods methods, MethodRelations relations) {
        MethodDeclarations modelBoolQueryMethods =  methods.modelBoolQueryMethods();
        List<BoolQueryModelMethodAngle> list = modelBoolQueryMethods.list().stream()
                .map(method -> new BoolQueryModelMethodAngle(
                        method,
                        relations.stream().filterTo(method).fromMethods()
                )).collect(toList());
        return new BoolQueryModelMethodAngles(list);
    }

    public List<BoolQueryModelMethodAngle> list() {
        return values;
    }
}
