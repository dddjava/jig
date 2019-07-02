package org.dddjava.jig.domain.model.collections;

import org.dddjava.jig.domain.model.businessrules.BusinessRuleFields;
import org.dddjava.jig.domain.model.declaration.field.FieldType;

public class CollectionField {
    BusinessRuleFields businessRuleFields;

    public CollectionField(BusinessRuleFields businessRuleFields) {
        this.businessRuleFields = businessRuleFields;
    }

    public FieldType fieldType() {
        return businessRuleFields.fieldType();
    }
}
