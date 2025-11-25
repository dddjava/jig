package org.dddjava.jig.domain.model.knowledge.usecases;

import org.dddjava.jig.domain.model.information.types.JigType;
import org.junit.jupiter.api.Test;
import testing.TestSupport;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StringComparingMethodListTest {

    @Test
    void String比較を行っていないクラス() {
        JigType jigType = TestSupport.buildJigType(NonStringComparingClass.class);

        StringComparingMethodList sut = StringComparingMethodList.from(jigType.allJigMethodStream());

        assertTrue(sut.list().isEmpty());
    }

    @Test
    void String比較を行っているクラス() {
        JigType jigType = TestSupport.buildJigType(StringComparingClass.class);

        StringComparingMethodList sut = StringComparingMethodList.from(jigType.allJigMethodStream());

        assertEquals(2, sut.list().size());
        assertEquals("stringEqualsMethod1", sut.list().get(0).name());
        assertEquals("stringEqualsMethod2", sut.list().get(1).name());
    }

    private static class NonStringComparingClass {
    }

    private static class StringComparingClass {
        boolean stringEqualsMethod1(String args) {
            return "hoge".equals(args);
        }

        boolean stringEqualsMethod2(String args) {
            return args.equals("hoge");
        }

        // 検出できていない
        boolean stringEqualsMethod3(String args) {
            Object hoge = "hoge";
            return hoge.equals(args);
        }

        // 検出できていない
        boolean objectEqualsMethod(String args) {
            return Objects.equals("hoge", args);
        }
    }
}