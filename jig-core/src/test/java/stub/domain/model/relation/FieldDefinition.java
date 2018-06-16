package stub.domain.model.relation;

import stub.domain.model.relation.field.*;

import java.util.List;

public class FieldDefinition {

    static StaticField staticField;

    InstanceField instanceField;

    List<GenericField> genericFields;

    ArrayField[] arrayFields;

    @FieldAnnotation
    Object obj = ReferenceConstantOwnerAtFieldDefinition.FIELD;
}
