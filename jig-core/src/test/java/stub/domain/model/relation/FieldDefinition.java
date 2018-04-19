package stub.domain.model.relation;

import stub.domain.model.relation.test.*;

import java.util.List;

public class FieldDefinition {

    static StaticField staticField;

    InstanceField instanceField;

    List<GenericField> genericFields;

    ArrayField[] arrayFields;

    @RetentionClassAnnotation
    Object obj = FieldReference.FIELD;
}
