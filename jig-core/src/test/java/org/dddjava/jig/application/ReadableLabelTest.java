package org.dddjava.jig.application;

import org.dddjava.jig.domain.model.data.packages.PackageId;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.information.JigRepository;
import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.types.JigType;
import org.dddjava.jig.domain.model.information.types.JigTypes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import stub.domain.model.ClassJavadocStub;
import stub.domain.model.MethodJavadocStub;
import stub.domain.model.NotJavadocStub;
import testing.JigServiceTest;

import java.util.NoSuchElementException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;

@JigServiceTest
class ReadableLabelTest {

    JigDocumentContext sut;

    public ReadableLabelTest(JigDocumentContext jigDocumentContext) {
        sut = jigDocumentContext;
    }

    @Test
    void パッケージ別名取得() {
        assertEquals("テストでJIGに読み取らせる実装", sut.packageTerm(PackageId.valueOf("stub")).title());
    }

    @ParameterizedTest
    @MethodSource
    void クラスコメント取得(Class<?> targetClass, String expectedText, JigRepository jigRepository) {
        var jigTypes = jigRepository.fetchJigTypes();
        String label = jigTypes.resolveJigType(TypeId.from(targetClass))
                .map(jigType -> jigType.label())
                .orElseThrow(AssertionError::new);

        assertEquals(expectedText, label);
    }

    static Stream<Arguments> クラスコメント取得() {
        return Stream.of(
                argumentSet("クラスのJavadocがClassCommentになるできる", ClassJavadocStub.class, "クラスのJavadoc"),
                argumentSet("メソッドのJavadocはClassCommentではない", MethodJavadocStub.class, "MethodJavadocStub"),
                argumentSet("ブロックコメントはClassCommentではない", NotJavadocStub.class, "NotJavadocStub")
        );
    }

    @Test
    void メソッド別名取得(JigRepository jigRepository) {
        JigTypes jigTypes = jigRepository.fetchJigTypes();
        TypeId テスト対象クラス = TypeId.from(MethodJavadocStub.class);
        JigType jigType = jigTypes.listMatches(item -> item.id().equals(テスト対象クラス)).get(0);

        JigMethod method = resolveMethodBySignature(jigType, "method()");
        assertEquals("メソッドのJavadoc", method.aliasTextOrBlank());

        JigMethod overloadedMethod = resolveMethodBySignature(jigType, "overloadMethod(String)");
        assertTrue(overloadedMethod.aliasTextOrBlank().matches("引数ありのメソッド"));

        JigMethod overloadedMethod2 = resolveMethodBySignature(jigType, "overloadMethod()");
        assertTrue(overloadedMethod2.aliasTextOrBlank().matches("引数なしのメソッド"));
    }

    JigMethod resolveMethodBySignature(JigType jigType, String methodText) {
        return jigType.allJigMethodStream()
                .filter(jigMethod -> jigMethod.nameAndArgumentSimpleText().equals(methodText))
                .findFirst()
                .orElseThrow(NoSuchElementException::new);
    }
}
