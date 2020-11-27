package org.dddjava.jig.application.service;

import org.assertj.core.api.Assertions;
import org.dddjava.jig.domain.model.jigmodel.jigtype.member.JigMethod;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.alias.TypeAlias;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.method.Arguments;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.method.MethodIdentifier;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.method.MethodSignature;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.package_.PackageIdentifier;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.jigsource.file.Sources;
import org.dddjava.jig.domain.model.jigsource.jigloader.analyzed.MethodFact;
import org.dddjava.jig.domain.model.jigsource.jigloader.analyzed.TypeFacts;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import stub.domain.model.ClassJavadocStub;
import stub.domain.model.MethodJavadocStub;
import stub.domain.model.NotJavadocStub;
import testing.JigServiceTest;

import java.util.Collections;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JigServiceTest
class DocumentationCommentServiceTest {

    AliasService sut;
    JigSourceReadService jigSourceReadService;

    public DocumentationCommentServiceTest(AliasService aliasService, JigSourceReadService jigSourceReadService) {
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
        TypeAlias typeAlias = typeFacts.listJigTypes()
                .stream().filter(jigType -> jigType.identifier().equals(typeIdentifier))
                .map(jigType -> jigType.typeAlias())
                .findAny().orElseThrow(AssertionError::new);

        assertEquals(comment, typeAlias.asText());
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
        JigMethod method = typeFacts.instanceMethodFacts().stream()
                .filter(e -> e.methodIdentifier().equals(new MethodIdentifier(
                        new TypeIdentifier(MethodJavadocStub.class),
                        new MethodSignature("method", new Arguments(Collections.emptyList())))))
                .map(MethodFact::createMethod)
                .findAny().orElseThrow(AssertionError::new);
        assertEquals("メソッドのJavadoc", method.aliasTextOrBlank());

        JigMethod overloadedMethod = typeFacts.instanceMethodFacts().stream()
                .filter(e -> e.methodIdentifier().equals(new MethodIdentifier(
                        new TypeIdentifier(MethodJavadocStub.class),
                        new MethodSignature("overloadMethod", new Arguments(Collections.singletonList(new TypeIdentifier(String.class)))))))
                .map(MethodFact::createMethod)
                .findAny().orElseThrow(AssertionError::new);
        assertTrue(overloadedMethod.aliasTextOrBlank().matches("引数(なし|あり)のメソッド"));

        JigMethod overloadedMethod2 = typeFacts.instanceMethodFacts().stream()
                .filter(e -> e.methodIdentifier().equals(new MethodIdentifier(
                        new TypeIdentifier(MethodJavadocStub.class),
                        new MethodSignature("overloadMethod", new Arguments(Collections.emptyList())))))
                .map(MethodFact::createMethod)
                .findAny().orElseThrow(AssertionError::new);
        assertTrue(overloadedMethod2.aliasTextOrBlank().matches("引数(なし|あり)のメソッド"));
    }
}
