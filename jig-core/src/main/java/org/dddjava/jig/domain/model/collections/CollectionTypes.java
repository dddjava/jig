package org.dddjava.jig.domain.model.collections;

import org.dddjava.jig.domain.model.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCode;
import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCodes;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * コレクション一覧
 */
public class CollectionTypes {

    List<CollectionType> list;

    public CollectionTypes(TypeByteCodes typeByteCodes) {
        list = new ArrayList<>();
        for (TypeByteCode typeByteCode : typeByteCodes.list()) {
            // TODO businessRuleに限定する

            FieldDeclarations fieldDeclarations = typeByteCode.fieldDeclarations();
            if (fieldDeclarations.matches(new TypeIdentifier(List.class))
                    || fieldDeclarations.matches(new TypeIdentifier(Set.class))) {
                list.add(new CollectionType(typeByteCode));
            }
        }
    }

    public List<CollectionType> list() {
        return list;
    }
}
