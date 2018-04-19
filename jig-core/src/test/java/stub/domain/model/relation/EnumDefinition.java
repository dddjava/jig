package stub.domain.model.relation;

import stub.domain.model.relation.test.ClassReference;
import stub.domain.model.relation.test.ConstructorArgument;
import stub.domain.model.relation.test.InstanceField;

public enum EnumDefinition {

    要素1(null, ClassReference.class),
    要素2(null, String.class);

    InstanceField field;

    EnumDefinition(ConstructorArgument arg, Class<?> clz) {
    }
}
