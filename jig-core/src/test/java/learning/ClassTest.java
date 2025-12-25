package learning;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClassTest {

    static class NestedClass {
    }

    @Test
    void getName() {
        var name = ClassTest.class.getName();

        assertEquals("learning.ClassTest", name);
    }

    @Test
    void getName_Nested() {
        var name = ClassTest.NestedClass.class.getName();

        assertEquals("learning.ClassTest$NestedClass", name);
    }

    @Test
    void getCanonicalName_Nested() {
        var name = ClassTest.NestedClass.class.getCanonicalName();

        assertEquals("learning.ClassTest.NestedClass", name);
    }

    @Test
    void getSimpleName_Nested() {
        var name = ClassTest.NestedClass.class.getSimpleName();

        assertEquals("NestedClass", name);
    }

    @Test
    void getTypeName_Nested() {
        var name = ClassTest.NestedClass.class.getTypeName();

        assertEquals("learning.ClassTest$NestedClass", name);
    }

    @Test
    void getPackageName_Nested() {
        var name = ClassTest.NestedClass.class.getPackageName();

        assertEquals("learning", name);
    }

    @Test
    void getName_Nested_Array() {
        var name = ClassTest.NestedClass[].class.getName();

        assertEquals("[Llearning.ClassTest$NestedClass;", name);
    }

    @Test
    void getTypeName_Nested_Array() {
        var name = ClassTest.NestedClass[].class.getTypeName();

        assertEquals("learning.ClassTest$NestedClass", name);
    }
}
