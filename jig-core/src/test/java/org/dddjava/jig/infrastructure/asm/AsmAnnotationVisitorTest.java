package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.data.types.JigAnnotationReference;
import org.dddjava.jig.domain.model.sources.classsources.ClassDeclaration;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AsmAnnotationVisitorTest {

    private @interface MyAnnotation {

        String string() default "";

        String[] arrayString() default {};

        int number() default 0;

        Class<?> clz() default Object.class;

        Class<?>[] arrayClz() default {};

        MyEnum enumValue() default MyEnum.AAA;
    }

    private @interface MyAnnotation2 {
        MyAnnotation annotation() default @MyAnnotation;

        MyAnnotation[] arrayAnnotation() default {};
    }

    private enum MyEnum {
        AAA, BBB;
    }

    @MyAnnotation2(annotation = @MyAnnotation(string = "foo"))
    @MyAnnotation(string = "hoge", arrayString = {"fuga", "piyo"}, number = 9, clz = String.class, arrayClz = {Integer.class}, enumValue = MyEnum.BBB)
    private static class AnnotatedSut {

    }

    @Test
    void name() {
        AsmClassVisitor visitor = AsmClassVisitorTest.asmClassVisitor(AnnotatedSut.class);
        ClassDeclaration classDeclaration = visitor.classDeclaration();

        var sut = classDeclaration.jigTypeHeader()
                .jigTypeAttributeData().declarationAnnotationList();

        assertEquals(2, sut.size());

        JigAnnotationReference myAnnotation = sut.get(0);
        assertEquals("MyAnnotation", myAnnotation.id().asSimpleName());

        assertEquals(6, myAnnotation.elements().size());
        assertEquals("hoge", myAnnotation.elementOf("string").orElseThrow());
        assertEquals(List.of("fuga", "piyo").toString(), myAnnotation.elementOf("arrayString").orElseThrow());
        assertEquals("9", myAnnotation.elementOf("number").orElseThrow());
        assertEquals("Ljava/lang/String;", myAnnotation.elementOf("clz").orElseThrow());
        assertEquals("Ljava/lang/Integer;", myAnnotation.elementOf("arrayClz").orElseThrow());
        assertEquals("BBB", myAnnotation.elementOf("enumValue").orElseThrow());

        JigAnnotationReference myAnnotation1 = sut.get(1);
        assertEquals("MyAnnotation2", myAnnotation1.id().asSimpleName());
        assertEquals(1, myAnnotation1.elements().size());
        assertEquals("Lorg/dddjava/jig/infrastructure/asm/AsmAnnotationVisitorTest$MyAnnotation;[...]", myAnnotation1.elementOf("annotation").orElseThrow());
    }
}