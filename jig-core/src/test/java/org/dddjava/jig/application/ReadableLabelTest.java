package org.dddjava.jig.application;

import org.dddjava.jig.domain.model.data.classes.method.JigMethod;
import org.dddjava.jig.domain.model.data.classes.method.MethodSignature;
import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.information.JigTypesRepository;
import org.dddjava.jig.domain.model.information.type.JigType;
import org.dddjava.jig.domain.model.information.type.JigTypes;
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
        assertEquals("テストでJIGに読み取らせる実装", sut.packageTerm(PackageIdentifier.valueOf("stub")).title());
    }

    @ParameterizedTest
    @MethodSource
    void クラスコメント取得(Class<?> targetClass, String expectedText, JigTypesRepository jigTypesRepository) {
        var jigTypes = jigTypesRepository.fetchJigTypes();
        String label = jigTypes.resolveJigType(TypeIdentifier.from(targetClass))
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
    void メソッド別名取得(JigTypesRepository jigTypesRepository) {
        JigTypes jigTypes = jigTypesRepository.fetchJigTypes();
        TypeIdentifier テスト対象クラス = TypeIdentifier.from(MethodJavadocStub.class);
        JigType jigType = jigTypes.listMatches(item -> item.identifier().equals(テスト対象クラス)).get(0);

        JigMethod method = resolveMethodBySignature(jigType, new MethodSignature("method"));
        assertEquals("メソッドのJavadoc", method.aliasTextOrBlank());

        JigMethod overloadedMethod = resolveMethodBySignature(jigType, new MethodSignature("overloadMethod", TypeIdentifier.from(String.class)));
        assertTrue(overloadedMethod.aliasTextOrBlank().matches("引数ありのメソッド"));

        JigMethod overloadedMethod2 = resolveMethodBySignature(jigType, new MethodSignature("overloadMethod"));
        assertTrue(overloadedMethod2.aliasTextOrBlank().matches("引数なしのメソッド"));
    }

    JigMethod resolveMethodBySignature(JigType jigType, MethodSignature methodSignature) {
        return jigType.allJigMethodStream()
                .filter(jigMethod -> jigMethod.declaration().methodSignature().isSame(methodSignature))
                .findFirst()
                .orElseThrow(NoSuchElementException::new);
    }
}
