package org.dddjava.jig.domain.model.information.types;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMapping;
import testing.TestSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JigTypeTest {

    @Test
    void アノテーションの値取得_String() {
        var jigType = TestSupport.buildJigType(MyAnnotatedClass.class);

        var actual = jigType.annotationValueOf(TestSupport.getTypeIdFromClass(RequestMapping.class), "name")
                .orElseThrow(); // 取れなければテスト失敗でよい

        assertEquals("hoge", actual);
    }

    @Test
    void アノテーションの値取得_String配列の単一値() {
        var jigType = TestSupport.buildJigType(MyAnnotatedClass.class);

        var actual = jigType.annotationValueOf(TestSupport.getTypeIdFromClass(RequestMapping.class), "value")
                .orElseThrow(); // 取れなければテスト失敗でよい

        assertEquals("{id}", actual);
    }

    @Test
    void アノテーションの値取得_aliasなど片方だけとればいいものは最初にマッチしたものがとれる() {
        var jigType = TestSupport.buildJigType(MyAnnotatedClass.class);

        var actual = jigType.annotationValueOf(TestSupport.getTypeIdFromClass(RequestMapping.class), "none", "value", "path")
                .orElseThrow(); // 取れなければテスト失敗でよい

        // pathの値がとれている。
        // つまり引数の順ではなくアノテーションに書いた順番になっているが、一つしか指定できないものでの使用を想定しているのでよしとする。
        assertEquals("/hoge", actual);
    }

    @RequestMapping(name = "hoge", path = "/hoge", value = "{id}")
    private static class MyAnnotatedClass {
    }
}