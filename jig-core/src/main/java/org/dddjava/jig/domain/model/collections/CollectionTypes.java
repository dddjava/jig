package org.dddjava.jig.domain.model.collections;

import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCode;
import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCodes;
import org.dddjava.jig.domain.model.values.ValueKind;

import java.util.ArrayList;
import java.util.List;

public class CollectionTypes {

    List<CollectionType> list;

    public CollectionTypes(TypeByteCodes typeByteCodes) {
        list = new ArrayList<>();
        for (TypeByteCode typeByteCode : typeByteCodes.list()) {
            if (!ValueKind.COLLECTION.matches(typeByteCode.fieldDeclarations())) {
                continue;
            }
            // TODO businessRuleに限定する

            list.add(new CollectionType(typeByteCode));
        }
    }

    public List<CollectionType> list() {
        return list;
    }
}
