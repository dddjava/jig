package org.dddjava.jig.domain.model.collections;

import org.dddjava.jig.domain.model.implementation.architecture.Architecture;
import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCode;
import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCodes;
import org.dddjava.jig.domain.model.implementation.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.implementation.declaration.type.TypeIdentifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * コレクション一覧
 */
public class CollectionTypes {

    List<CollectionType> list;

    public CollectionTypes(TypeByteCodes typeByteCodes, Architecture architecture) {
        list = new ArrayList<>();
        for (TypeByteCode typeByteCode : typeByteCodes.list()) {
            if (!architecture.isBusinessRule(typeByteCode.typeIdentifier())) {
                continue;
            }

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
