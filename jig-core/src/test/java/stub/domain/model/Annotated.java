package stub.domain.model;

import stub.domain.model.relation.test.UseInAnnotation;
import stub.domain.model.relation.test.VariableAnnotation;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Annotated {

    @VariableAnnotation(string = "af", arrayString = "bf", number = 13, clz = Field.class, arrayClz = {Object.class, Object.class}, enumValue = UseInAnnotation.DUMMY1, annotation = @Deprecated)
    Object field;

    @VariableAnnotation(string = "am", arrayString = "bm", number = 23, clz = Method.class, enumValue = UseInAnnotation.DUMMY2)
    void method() {
    }
}
