package org.dddjava.jig.application.service;

import org.assertj.core.api.Assertions;
import org.dddjava.jig.domain.model.declaration.method.MethodIdentifier;
import org.dddjava.jig.domain.model.declaration.method.MethodSignature;
import org.dddjava.jig.domain.model.declaration.package_.PackageIdentifier;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.source.Sources;
import org.dddjava.jig.domain.model.implementation.source.code.AliasSource;
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
class AliasServiceTest {

    AliasService sut;

    public AliasServiceTest(AliasService aliasService) {
        sut = aliasService;
    }

    @Test
    void パッケージ別名取得(Sources source) {
        AliasSource aliasSource = source.aliasSource();

        sut.loadAliases(aliasSource);

        Assertions.assertThat(sut.packageAliasOf(new PackageIdentifier("stub")).asText())
                .isEqualTo("テストで使用するスタブたち");
    }

    @ParameterizedTest
    @MethodSource
    void クラス別名取得(TypeIdentifier typeIdentifier, String comment, Sources source) {
        AliasSource aliasSource = source.aliasSource();

        sut.loadAliases(aliasSource);

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
    void メソッド別名取得(Sources source) {
        AliasSource aliasSource = source.aliasSource();

        sut.loadAliases(aliasSource);

        MethodIdentifier methodIdentifier = new MethodIdentifier(new TypeIdentifier(MethodJavadocStub.class), new MethodSignature(
                "method",
                new org.dddjava.jig.domain.model.declaration.method.Arguments(Collections.emptyList())));
        Assertions.assertThat(sut.methodAliasOf(methodIdentifier).asText())
                .isEqualTo("メソッドのJavadoc");

        MethodIdentifier overloadMethodIdentifier = new MethodIdentifier(new TypeIdentifier(MethodJavadocStub.class), new MethodSignature(
                "overloadMethod",
                new org.dddjava.jig.domain.model.declaration.method.Arguments(Collections.singletonList(new TypeIdentifier(String.class)))));
        Assertions.assertThat(sut.methodAliasOf(overloadMethodIdentifier).asText())
                // オーバーロードは一意にならないのでどちらか
                .matches("引数(なし|あり)のメソッド");
    }
}
