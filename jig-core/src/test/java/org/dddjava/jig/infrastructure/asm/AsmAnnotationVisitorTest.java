package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.data.types.JigAnnotationReference;
import org.junit.jupiter.api.Test;
import testing.TestSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AsmAnnotationVisitorTest {

    private @interface MyAnnotation {

        String string() default "";

        String[] arrayString() default {};

        int number() default 0;

        Class<?> clz() default Object.class;

        Class<?>[] arrayClz() default {};

        MyEnum enumValue() default MyEnum.AAA;

        MyEnum[] arrayEnumValue() default {};
    }

    private @interface MyAnnotation2 {
        MyAnnotation annotation() default @MyAnnotation;

        MyAnnotation[] arrayAnnotation() default {};
    }

    private enum MyEnum {
        AAA, BBB;
    }

    @MyAnnotation2(
            annotation = @MyAnnotation(string = "foo"),
            arrayAnnotation = {
                    @MyAnnotation(string = "array1"),
                    @MyAnnotation(string = "array2")
            }
    )
    @MyAnnotation(string = "hoge", number = 9, clz = String.class, enumValue = MyEnum.BBB,
            arrayString = {"fuga", "piyo"},
            arrayClz = {Integer.class},
            arrayEnumValue = {MyEnum.BBB, MyEnum.AAA}
    )
    private static class AnnotatedSut {

    }

    @Test
    void name() {
        var sut = TestSupport.buildJigType(AnnotatedSut.class)
                .jigTypeHeader().jigTypeAttributes().declarationAnnotationList();

        assertEquals(2, sut.size());

        JigAnnotationReference myAnnotation = sut.get(0);
        assertEquals("MyAnnotation", myAnnotation.id().asSimpleName());

        assertEquals(7, myAnnotation.elements().size());
        assertEquals("hoge", myAnnotation.elementTextOf("string").orElseThrow());
        assertEquals("{fuga, piyo}", myAnnotation.elementTextOf("arrayString").orElseThrow());
        assertEquals("9", myAnnotation.elementTextOf("number").orElseThrow());
        assertEquals("String", myAnnotation.elementTextOf("clz").orElseThrow());
        assertEquals("Integer", myAnnotation.elementTextOf("arrayClz").orElseThrow());
        assertEquals("MyEnum.BBB", myAnnotation.elementTextOf("enumValue").orElseThrow());
        assertEquals("{MyEnum.BBB, MyEnum.AAA}", myAnnotation.elementTextOf("arrayEnumValue").orElseThrow());

        JigAnnotationReference myAnnotation1 = sut.get(1);
        assertEquals("MyAnnotation2", myAnnotation1.id().asSimpleName());
        assertEquals(2, myAnnotation1.elements().size());
        // TODO アノテーションのネストは未対応のため省略表示
        assertEquals("@MyAnnotation(...)", myAnnotation1.elementTextOf("annotation").orElseThrow());
    }
}