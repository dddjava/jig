package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.data.members.instruction.MethodCall;
import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.junit.jupiter.api.Test;
import testing.TestSupport;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AsmMethodVisitorInstructionTest {

    private static class SutClass {
        public void 自クラスメソッド参照メソッド() {
            Stream.of(1)
                    .map(this::メソッド参照で呼ばれるメソッド1)
                    .close();
        }

        private Character メソッド参照で呼ばれるメソッド1(long l) {
            return 'c';
        }

        public void 他クラスメソッド参照メソッド() {
            Stream.of(1)
                    .map(AnotherClass::メソッド参照で呼ばれるメソッド2)
                    .close();
        }

        public void lambda式メソッド() {
            Stream.of(1)
                    .map(i -> {
                        return "hoge";
                    })
                    .close();
        }
    }

    private static class AnotherClass {
        static CharSequence メソッド参照で呼ばれるメソッド2(int i) {
            return "anotherMethod";
        }
    }

    @Test
    void 自身のメソッド参照のInstructionが検出できる() {
        JigMethod jigMethod = TestSupport.JigMethod準備(SutClass.class, "自クラスメソッド参照メソッド");

        MethodCall actual = jigMethod.instructions().invokeDynamicInstructionStream()
                .flatMap(instruction -> instruction.findMethodCall())
                .findAny().orElseThrow();

        assertEquals("メソッド参照で呼ばれるメソッド1", actual.methodName());
    }

    @Test
    void 違うクラスのメソッド参照のInstructionが検出できる() {
        JigMethod jigMethod = TestSupport.JigMethod準備(SutClass.class, "他クラスメソッド参照メソッド");

        MethodCall actual = jigMethod.instructions().invokeDynamicInstructionStream()
                .flatMap(instruction -> instruction.findMethodCall())
                .findAny().orElseThrow();

        assertEquals("メソッド参照で呼ばれるメソッド2", actual.methodName());
    }

    @Test
    void Lambda式のInstructionが検出できる() {
        JigMethod jigMethod = TestSupport.JigMethod準備(SutClass.class, "lambda式メソッド");

        MethodCall actual = jigMethod.instructions().invokeDynamicInstructionStream()
                .flatMap(instruction -> instruction.findMethodCall())
                .findAny().orElseThrow();

        assertEquals("lambda$lambda式メソッド$0", actual.methodName());
    }
}
