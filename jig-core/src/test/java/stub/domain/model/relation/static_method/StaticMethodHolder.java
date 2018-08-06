package stub.domain.model.relation.static_method;

import stub.domain.model.relation.static_method.to_constant_field.ConstantdHolder;

public class StaticMethodHolder {

    static String something() {
        return ConstantdHolder.STATIC_STRING_CONSTANT;
    }
}
