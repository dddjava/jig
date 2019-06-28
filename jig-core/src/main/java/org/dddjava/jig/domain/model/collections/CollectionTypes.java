package org.dddjava.jig.domain.model.collections;

import org.dddjava.jig.domain.model.businessrules.BusinessRule;
import org.dddjava.jig.domain.model.businessrules.BusinessRules;
import org.dddjava.jig.domain.model.declaration.field.FieldDeclaration;
import org.dddjava.jig.domain.model.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * コレクション一覧
 */
public class CollectionTypes {

    List<CollectionType> list;

    public CollectionTypes(BusinessRules businessRules) {
        list = new ArrayList<>();
        for (BusinessRule businessRule : businessRules.list()) {
            FieldDeclarations fieldDeclarations = businessRule.typeByteCode().fieldDeclarations();

            if (fieldDeclarations.matches(new TypeIdentifier(List.class))
                    || fieldDeclarations.matches(new TypeIdentifier(Set.class))) {
                FieldDeclaration fieldDeclaration = fieldDeclarations.onlyOneField();
                list.add(new CollectionType(businessRule, new CollectionField(fieldDeclaration)));
            }
        }
    }

    public List<CollectionType> list() {
        return list;
    }
}
