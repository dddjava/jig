package stub.domain.model.relation.static_method;

import stub.domain.model.relation.static_method.to_int_constant_field.IntConstantFieldHolder;
import stub.domain.model.relation.static_method.to_integer_constant_field.IntegerConstantFieldHolder;
import stub.domain.model.relation.static_method.to_static_field.StaticFieldHolder;
import stub.domain.model.relation.static_method.to_string_constant_field.StringConstantdHolder;

public class StaticMethodHolder {

    static String accessStaticStringConstant() {
        return StringConstantdHolder.STATIC_STRING_CONSTANT;
    }

    static String accessStaticField() {
        return StaticFieldHolder.STATIC_FIELD;
    }

    static int accessStaticIntConstant() {
        return IntConstantFieldHolder.INT_CONSTANT;
    }

    static Integer acecssStaticIntegerConstant() {
        return IntegerConstantFieldHolder.INTEGER_CONSTANT;
    }
}
