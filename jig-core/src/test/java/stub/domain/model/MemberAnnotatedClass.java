package stub.domain.model;

import stub.domain.model.relation.annotation.UseInAnnotation;
import stub.domain.model.relation.annotation.VariableAnnotation;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class MemberAnnotatedClass {

    @VariableAnnotation(string = "af", arrayString = "bf", number = 13, clz = Field.class, arrayClz = {Object.class, Object.class}, enumValue = UseInAnnotation.DUMMY1, annotation = @Deprecated)
    Object field;

    @VariableAnnotation(string = "am", arrayString = {"bm1", "bm2"}, number = 23, clz = Method.class, enumValue = UseInAnnotation.DUMMY2)
    void method() {
    }
}
