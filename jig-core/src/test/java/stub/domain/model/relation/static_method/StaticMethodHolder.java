package stub.domain.model.relation.static_method;

import stub.domain.model.relation.static_method.to_constant_field.ConstantdHolder;
import stub.domain.model.relation.static_method.to_static_field.StaticFieldHolder;

public class StaticMethodHolder {

    static String accessStaticConstant() {
        return ConstantdHolder.STATIC_STRING_CONSTANT;
    }

    static String accessStaticField() {
        return StaticFieldHolder.STATIC_FIELD;
    }
}
