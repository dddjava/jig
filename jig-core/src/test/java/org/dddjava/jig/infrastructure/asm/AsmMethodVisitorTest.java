package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.data.classes.annotation.AnnotationDescription;
import org.dddjava.jig.domain.model.data.classes.annotation.MethodAnnotation;
import org.dddjava.jig.domain.model.data.classes.method.JigMethod;
import org.dddjava.jig.domain.model.data.members.JigMethodHeader;
import org.dddjava.jig.domain.model.information.type.JigTypeMembers;
import org.dddjava.jig.domain.model.sources.classsources.JigMemberBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.objectweb.asm.ClassReader;
import stub.domain.model.relation.annotation.UseInAnnotation;
import stub.domain.model.relation.annotation.VariableAnnotation;
import stub.misc.DecisionClass;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * MethodVisitorはClassVisitor経由でテストする
 */
class AsmMethodVisitorTest {

    /**
     * テストで読み取るメソッドを定義したクラス
     */
    private static class MethodVisitorSut {
        void 引数型のジェネリクスが取得できる(List<String> list) {
        }

        List<String> 戻り値のジェネリクスが取得できる() {
            return null;
        }

        @VariableAnnotation(string = "am", arrayString = {"bm1", "bm2" }, number = 23, clz = Method.class, enumValue = UseInAnnotation.DUMMY2)
        void メソッドに付与されているアノテーションと記述が取得できる() {
        }
    }

    @Test
    void メソッドに付与されているアノテーションと記述が取得できる() throws Exception {
        JigMethod method = JigMethod準備(MethodVisitorSut.class, "メソッドに付与されているアノテーションと記述が取得できる");
        MethodAnnotation methodAnnotation = method.methodAnnotations().list().get(0);

        assertThat(methodAnnotation.annotationType().fullQualifiedName()).isEqualTo(VariableAnnotation.class.getTypeName());

        AnnotationDescription description = methodAnnotation.description();
        assertThat(description.asText())
                .contains(
                        "string=am",
                        "arrayString=[bm1, bm2]",
                        "number=23",
                        "clz=Ljava/lang/reflect/Method;",
                        "enumValue=DUMMY2"
                );
    }

    @Test
    void 戻り値のジェネリクスが取得できる() throws Exception {
        JigMethod actual = JigMethod準備(MethodVisitorSut.class, "戻り値のジェネリクスが取得できる");

        assertEquals("List<String>", actual.declaration().methodReturn().parameterizedType().asSimpleText());
    }

    @Test
    void 引数型のジェネリクスが取得できる() {
        JigMethod actual = JigMethod準備(MethodVisitorSut.class, "引数型のジェネリクスが取得できる");

        assertEquals("引数型のジェネリクスが取得できる(java.util.List<java.lang.String>)", actual.declaration().methodSignature().asText());
    }

    @CsvSource({
            "分岐なしメソッド, 0",
            "ifがあるメソッド, 1",
            "switchがあるメソッド, 1",
            // forは ifeq と goto で構成されるある意味での分岐
            "forがあるメソッド, 1",
    })
    @ParameterizedTest
    void メソッドでifやswitchを使用していると検出できる(String name, int number) throws Exception {
        JigMethod actual = JigMethod準備(DecisionClass.class, name);
        assertEquals(number, actual.decisionNumber().intValue());
    }

    private static JigMethod JigMethod準備(Class<?> sutClass, String methodName) {
        JigTypeMembers members = 準備(sutClass).buildJigTypeMembers();
        return members.jigMethodStream()
                .filter(jigMethod -> jigMethod.name().equals(methodName))
                .findFirst()
                .orElseThrow();
    }

    private static JigMethodHeader JigMethodHeader準備(Class<?> sutClass, String methodName) {
        var members = 準備(sutClass).buildJigTypeMembers();
        Collection<JigMethodHeader> methodByName = members.findMethodByName(methodName);
        return methodByName.stream().findFirst().orElseThrow();
    }

    private static JigMemberBuilder 準備(Class<?> sutClass) {
        try {
            AsmClassVisitor visitor = new AsmClassVisitor();
            new ClassReader(sutClass.getName()).accept(visitor, 0);
            return visitor.jigMemberBuilder();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}