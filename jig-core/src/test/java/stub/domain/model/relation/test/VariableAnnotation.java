package stub.domain.model.relation.test;


import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD})
public @interface VariableAnnotation {

    String string();

    String[] arrayString();

    int number();

    Class<?> clz();

    Class<?>[] arrayClz() default {};

    UseInAnnotation enumValue();

    Deprecated annotation() default @Deprecated;

    Deprecated[] arrayAnnotation() default {};
}
