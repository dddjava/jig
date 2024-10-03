package org.dddjava.jig.application;

import org.assertj.core.api.Assertions;
import org.dddjava.jig.domain.model.models.jigobject.class_.JigType;
import org.dddjava.jig.domain.model.models.jigobject.member.JigMethod;
import org.dddjava.jig.domain.model.models.jigobject.member.JigMethods;
import org.dddjava.jig.domain.model.parts.classes.method.MethodSignature;
import org.dddjava.jig.domain.model.parts.classes.type.ClassComment;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.parts.packages.PackageIdentifier;
import org.dddjava.jig.domain.model.sources.file.Sources;
import org.dddjava.jig.domain.model.sources.jigfactory.TypeFacts;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import stub.domain.model.ClassJavadocStub;
import stub.domain.model.MethodJavadocStub;
import stub.domain.model.NotJavadocStub;
import testing.JigServiceTest;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JigServiceTest
class AliasServiceTest {

    AliasService sut;
    JigSourceReadService jigSourceReadService;

    public AliasServiceTest(AliasService aliasService, JigSourceReadService jigSourceReadService) {
        sut = aliasService;
        this.jigSourceReadService = jigSourceReadService;
    }

    @Test
    void パッケージ別名取得(Sources source) {
        jigSourceReadService.readProjectData(source);

        Assertions.assertThat(sut.packageAliasOf(new PackageIdentifier("stub")).asText())
                .isEqualTo("テストで使用するスタブたち");
    }

    @ParameterizedTest
    @MethodSource
    void クラス別名取得(TypeIdentifier typeIdentifier, String comment, Sources source) {
        TypeFacts typeFacts = jigSourceReadService.readProjectData(source);
        ClassComment classComment = typeFacts.jigTypes().list()
                .stream().filter(jigType -> jigType.identifier().equals(typeIdentifier))
                .map(jigType -> jigType.typeAlias())
                .findAny().orElseThrow(AssertionError::new);

        assertEquals(comment, classComment.asText());
    }

    static Stream<org.junit.jupiter.params.provider.Arguments> クラス別名取得() {
        return Stream.of(
                org.junit.jupiter.params.provider.Arguments.of(new TypeIdentifier(ClassJavadocStub.class), "クラスのJavadoc"),
                org.junit.jupiter.params.provider.Arguments.of(new TypeIdentifier(MethodJavadocStub.class), ""),
                org.junit.jupiter.params.provider.Arguments.of(new TypeIdentifier(NotJavadocStub.class), "")
        );
    }

    @Test
    void メソッド別名取得(Sources source) {
        TypeFacts typeFacts = jigSourceReadService.readProjectData(source);
        TypeIdentifier テスト対象クラス = new TypeIdentifier(MethodJavadocStub.class);
        JigType jigType = typeFacts.jigTypes().listMatches(item -> item.identifier().equals(テスト対象クラス)).get(0);

        JigMethods jigMethods = jigType.instanceMember().instanceMethods();

        JigMethod method = jigMethods.resolveMethodBySignature(new MethodSignature("method"));
        assertEquals("メソッドのJavadoc", method.aliasTextOrBlank());

        JigMethod overloadedMethod = jigMethods.resolveMethodBySignature(new MethodSignature("overloadMethod", TypeIdentifier.of(String.class)));
        assertTrue(overloadedMethod.aliasTextOrBlank().matches("引数(なし|あり)のメソッド"));

        JigMethod overloadedMethod2 = jigMethods.resolveMethodBySignature(new MethodSignature("overloadMethod"));
        assertTrue(overloadedMethod2.aliasTextOrBlank().matches("引数(なし|あり)のメソッド"));
    }
}
