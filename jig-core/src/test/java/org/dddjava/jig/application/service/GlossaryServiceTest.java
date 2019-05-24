package org.dddjava.jig.application.service;

import org.assertj.core.api.Assertions;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.method.MethodIdentifier;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.method.MethodSignature;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.namespace.PackageIdentifier;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.raw.raw.RawSource;
import org.dddjava.jig.domain.model.implementation.raw.textfile.TextSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import stub.domain.model.*;
import testing.JigServiceTest;

import java.time.LocalDateTime;
import java.util.Arrays;
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
        TextSource textSource = source.textSource();

        sut.importJapanese(textSource.packageInfoSources());

        Assertions.assertThat(sut.japaneseNameFrom(new PackageIdentifier("stub")).value())
                .isEqualTo("テストで使用するスタブたち");
    }

    @ParameterizedTest
    @MethodSource
    void クラス別名取得(TypeIdentifier typeIdentifier, String comment, RawSource source) {
        TextSource textSource = source.textSource();

        sut.importJapanese(textSource.javaSources());
        sut.importJapanese(textSource.kotlinSources());

        Assertions.assertThat(sut.japaneseNameFrom(typeIdentifier).value())
                .isEqualTo(comment);
    }

    static Stream<Arguments> クラス別名取得() {
        return Stream.of(
                Arguments.of(new TypeIdentifier(ClassJavadocStub.class), "クラスのJavadoc"),
                Arguments.of(new TypeIdentifier(MethodJavadocStub.class), ""),
                Arguments.of(new TypeIdentifier(NotJavadocStub.class), ""),
                Arguments.of(new TypeIdentifier("DefaultPackageClass"), "デフォルトパッケージにあるクラス"),
                Arguments.of(new TypeIdentifier(KotlinStub.class), "KotlinのクラスのDoc")
        );
    }

    @Test
    void メソッド別名取得(RawSource source) {
        TextSource textSource = source.textSource();

        sut.importJapanese(textSource.javaSources());

        MethodIdentifier methodIdentifier = new MethodIdentifier(new TypeIdentifier(MethodJavadocStub.class), new MethodSignature(
                "method",
                new org.dddjava.jig.domain.model.implementation.analyzed.declaration.method.Arguments(Collections.emptyList())));
        Assertions.assertThat(sut.japaneseNameFrom(methodIdentifier).value())
                .isEqualTo("メソッドのJavadoc");

        MethodIdentifier overloadMethodIdentifier = new MethodIdentifier(new TypeIdentifier(MethodJavadocStub.class), new MethodSignature(
                "overloadMethod",
                new org.dddjava.jig.domain.model.implementation.analyzed.declaration.method.Arguments(Collections.singletonList(new TypeIdentifier(String.class)))));
        Assertions.assertThat(sut.japaneseNameFrom(overloadMethodIdentifier).value())
                // オーバーロードは一意にならないのでどちらか
                .matches("引数(なし|あり)のメソッド");
    }

    @Test
    void Kotlinメソッドの和名取得(RawSource source) {
        TextSource textSource = source.textSource();

        sut.importJapanese(textSource.kotlinSources());

        MethodIdentifier methodIdentifier = new MethodIdentifier(new TypeIdentifier(KotlinMethodJavadocStub.class), new MethodSignature(
                "simpleMethod",
                new org.dddjava.jig.domain.model.implementation.analyzed.declaration.method.Arguments(Collections.emptyList())));
        Assertions.assertThat(sut.japaneseNameFrom(methodIdentifier).value())
                .isEqualTo("メソッドのドキュメント");

        MethodIdentifier overloadMethodIdentifier1 = new MethodIdentifier(new TypeIdentifier(KotlinMethodJavadocStub.class), new MethodSignature(
                "overloadMethod",
                new org.dddjava.jig.domain.model.implementation.analyzed.declaration.method.Arguments(Collections.emptyList())));
        Assertions.assertThat(sut.japaneseNameFrom(overloadMethodIdentifier1).value())
                // オーバーロードは一意にならないのでどちらか
                .matches("引数(なし|あり)のメソッド");

        MethodIdentifier overloadMethodIdentifier2 = new MethodIdentifier(new TypeIdentifier(KotlinMethodJavadocStub.class), new MethodSignature(
                "overloadMethod",
                new org.dddjava.jig.domain.model.implementation.analyzed.declaration.method.Arguments(Arrays.asList(new TypeIdentifier(String.class), new TypeIdentifier(LocalDateTime.class)))));
        Assertions.assertThat(sut.japaneseNameFrom(overloadMethodIdentifier2).value())
                // オーバーロードは一意にならないのでどちらか
                .matches("引数(なし|あり)のメソッド");
    }
}
