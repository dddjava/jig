package org.dddjava.jig.domain.model.jigpresentation.collections;

import org.dddjava.jig.domain.model.declaration.field.FieldType;
import org.dddjava.jig.domain.model.jigmodel.businessrules.BusinessRuleFields;

public class CollectionField {
    BusinessRuleFields businessRuleFields;

    public CollectionField(BusinessRuleFields businessRuleFields) {
        this.businessRuleFields = businessRuleFields;
    }

    public FieldType fieldType() {
        return businessRuleFields.onlyOneFieldType();
    }
}
