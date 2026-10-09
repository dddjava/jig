package org.dddjava.jig.domain.model.knowledge.insight;

import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.types.JigType;
import org.dddjava.jig.domain.model.information.types.JigTypes;
import org.junit.jupiter.api.Test;
import testing.TestSupport;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MethodInsightTest {

    @Test
    void lambdaの中で参照したフィールドを外側のメソッドの使用フィールドに含める() {
        var sut = new MethodInsight(TestSupport.buildJigMethod(LambdaOwner.class, "filter"));

        assertEquals(1, sut.numberOfUsingFields());
        assertEquals(1, sut.numberOfUsingOwnFields());
    }

    @Test
    void lambdaの中の命令を外側のメソッドの規模に含める() {
        JigMethod method = TestSupport.buildJigMethod(LambdaOwner.class, "filter");

        var sut = new MethodInsight(method);

        JigMethod lambda = lambdaSyntheticMethodOf(LambdaOwner.class);
        // invokedynamic の1命令はそのまま、lambda の中身を足す
        assertEquals(method.instructions().instructions().size() + lambda.instructions().instructions().size(), sut.size());
    }

    @Test
    void lambdaの中で呼び出したメソッドを外側のメソッドの使用メソッドに含める() {
        var sut = new MethodInsight(TestSupport.buildJigMethod(LambdaOwner.class, "filter"));

        // lambda 内の this.accept(String) を自クラスのメソッド呼び出しとして数える。lambda 合成メソッドへの呼び出しは数えない
        assertEquals(1, sut.numberOfUsingOwnMethods());
    }

    @Test
    void lambda合成メソッドを判別できる() {
        assertTrue(lambdaSyntheticMethodOf(LambdaOwner.class).isLambdaSyntheticMethod());
    }

    @Test
    void lambdaの有無で凝集度欠如は変わらない() {
        assertEquals(lcom(LoopOwner.class), lcom(LambdaOwner.class));
    }

    private static JigMethod lambdaSyntheticMethodOf(Class<?> clz) {
        return TestSupport.buildJigType(clz).allJigMethodStream()
                .filter(jigMethod -> jigMethod.name().startsWith("lambda$"))
                .findFirst().orElseThrow();
    }

    private static double lcom(Class<?> clz) {
        JigTypes jigTypes = TestSupport.buildJigTypes(clz);
        JigType jigType = jigTypes.orderedStream().findFirst().orElseThrow();
        var methodInsights = jigType.allJigMethodStream().map(MethodInsight::new).toList();
        return new TypeInsight(jigType.id(), methodInsights).lcom(jigTypes);
    }

    @SuppressWarnings("unused")
    static class LambdaOwner {
        private int threshold;
        private String name;

        List<String> filter(List<String> values) {
            return values.stream().filter(value -> value.length() > threshold && accept(value)).toList();
        }

        boolean accept(String value) {
            return !value.isEmpty();
        }

        String name() {
            return name;
        }
    }

    @SuppressWarnings("unused")
    static class LoopOwner {
        private int threshold;
        private String name;

        List<String> filter(List<String> values) {
            List<String> result = new ArrayList<>();
            for (String value : values) {
                if (value.length() > threshold && accept(value)) result.add(value);
            }
            return result;
        }

        boolean accept(String value) {
            return !value.isEmpty();
        }

        String name() {
            return name;
        }
    }
}
