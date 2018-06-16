package stub.domain.model.relation;

import stub.domain.model.relation.enumeration.ClassReference;
import stub.domain.model.relation.enumeration.ConstructorArgument;
import stub.domain.model.relation.enumeration.EnumField;

public enum EnumDefinition {

    要素1(null, ClassReference.class),
    要素2(null, String.class);

    EnumField field;

    EnumDefinition(ConstructorArgument arg, Class<?> clz) {
    }
}
