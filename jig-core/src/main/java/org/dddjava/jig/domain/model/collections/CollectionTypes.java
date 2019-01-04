package org.dddjava.jig.domain.model.collections;

import org.dddjava.jig.domain.model.businessrules.BusinessRule;
import org.dddjava.jig.domain.model.businessrules.BusinessRules;
import org.dddjava.jig.domain.model.implementation.analyzed.bytecode.TypeByteCodes;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.type.TypeIdentifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * コレクション一覧
 */
public class CollectionTypes {

    List<CollectionType> list;

    public CollectionTypes(BusinessRules businessRules, TypeByteCodes typeByteCodes) {
        list = new ArrayList<>();
        for (BusinessRule businessRule: businessRules.list()) {
            TypeIdentifier identifier = businessRule.type().identifier();
            FieldDeclarations fieldDeclarations = typeByteCodes.typeByteCodeOf(identifier).fieldDeclarations();

            if (fieldDeclarations.matches(new TypeIdentifier(List.class))
                    || fieldDeclarations.matches(new TypeIdentifier(Set.class))) {
                list.add(new CollectionType(businessRule, typeByteCodes.typeByteCodeOf(identifier).methodDeclarations()));
            }
        }
    }

    public List<CollectionType> list() {
        return list;
    }
}
