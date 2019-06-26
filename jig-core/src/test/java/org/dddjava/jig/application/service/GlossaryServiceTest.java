package org.dddjava.jig.application.service;

import org.assertj.core.api.Assertions;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.method.MethodIdentifier;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.method.MethodSignature;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.package_.PackageIdentifier;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.raw.raw.RawSource;
import org.dddjava.jig.domain.model.implementation.raw.textfile.AliasSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import stub.domain.model.ClassJavadocStub;
import stub.domain.model.MethodJavadocStub;
import stub.domain.model.NotJavadocStub;
import testing.JigServiceTest;

import java.util.Collections;
import java.util.stream.Stream;

@JigServiceTest
class GlossaryServiceTest {

    GlossaryService sut;

    public GlossaryServiceTest(GlossaryService glossaryService) {
        sut = glossaryService;
    }

    @Test
    void パッケージ別名取得(RawSource source) {
        AliasSource aliasSource = source.textSource();

        sut.loadPackageAliases(aliasSource.packageInfoSources());

        Assertions.assertThat(sut.packageAliasOf(new PackageIdentifier("stub")).asText())
                .isEqualTo("テストで使用するスタブたち");
    }

    @ParameterizedTest
    @MethodSource
    void クラス別名取得(TypeIdentifier typeIdentifier, String comment, RawSource source) {
        AliasSource aliasSource = source.textSource();

        sut.loadAliases(aliasSource.javaSources());
        sut.loadAliases(aliasSource.kotlinSources());

        Assertions.assertThat(sut.typeAliasOf(typeIdentifier).asText())
                .isEqualTo(comment);
    }

    static Stream<Arguments> クラス別名取得() {
        return Stream.of(
                Arguments.of(new TypeIdentifier(ClassJavadocStub.class), "クラスのJavadoc"),
                Arguments.of(new TypeIdentifier(MethodJavadocStub.class), ""),
                Arguments.of(new TypeIdentifier(NotJavadocStub.class), ""),
                Arguments.of(new TypeIdentifier("DefaultPackageClass"), "デフォルトパッケージにあるクラス")
        );
    }

    @Test
    void メソッド別名取得(RawSource source) {
        AliasSource aliasSource = source.textSource();

        sut.loadAliases(aliasSource.javaSources());

        MethodIdentifier methodIdentifier = new MethodIdentifier(new TypeIdentifier(MethodJavadocStub.class), new MethodSignature(
                "method",
                new org.dddjava.jig.domain.model.implementation.analyzed.declaration.method.Arguments(Collections.emptyList())));
        Assertions.assertThat(sut.methodAliasOf(methodIdentifier).asText())
                .isEqualTo("メソッドのJavadoc");

        MethodIdentifier overloadMethodIdentifier = new MethodIdentifier(new TypeIdentifier(MethodJavadocStub.class), new MethodSignature(
                "overloadMethod",
                new org.dddjava.jig.domain.model.implementation.analyzed.declaration.method.Arguments(Collections.singletonList(new TypeIdentifier(String.class)))));
        Assertions.assertThat(sut.methodAliasOf(overloadMethodIdentifier).asText())
                // オーバーロードは一意にならないのでどちらか
                .matches("引数(なし|あり)のメソッド");
    }
}
