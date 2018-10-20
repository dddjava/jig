package stub.domain.model.category;

import java.util.function.Function;

public enum RelationEnum {

    A(Object.class, param -> ParameterizedEnum.A.name()),
    B(SimpleEnum.class, param -> null),
    C(Object.class, param -> null);

    RichEnum field;

    RelationEnum(Class<?> clz, Function<PolymorphismEnum, ?> function) {
        BehaviourEnum.A.method();
    }
}
