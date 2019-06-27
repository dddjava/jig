package org.dddjava.jig.application.service;

import org.assertj.core.api.Assertions;
import org.dddjava.jig.domain.model.implementation.analyzed.alias.SourceCodeJapaneseReader;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.method.MethodIdentifier;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.method.MethodSignature;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.raw.raw.RawSource;
import org.dddjava.jig.domain.model.implementation.raw.raw.RawSourceLocations;
import org.dddjava.jig.domain.model.implementation.raw.raw.TextSourceLocations;
import org.dddjava.jig.domain.model.implementation.raw.textfile.AliasSource;
import org.dddjava.jig.domain.model.implementation.source.binary.BinarySourceLocations;
import org.dddjava.jig.infrastructure.LocalFileRawSourceFactory;
import org.dddjava.jig.infrastructure.javaparser.JavaparserAliasReader;
import org.dddjava.jig.infrastructure.kotlin.KotlinparserJapaneseReaderSource;
import org.dddjava.jig.infrastructure.onmemoryrepository.OnMemoryAliasRepository;
import org.junit.jupiter.api.Test;
import stub.domain.model.KotlinMethodJavadocStub;
import stub.domain.model.KotlinStub;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

public class AliasServiceTest {

    AliasService sut;

    AliasServiceTest() {
        SourceCodeJapaneseReader sourceCodeJapaneseReader = new SourceCodeJapaneseReader(new JavaparserAliasReader(), new KotlinparserJapaneseReaderSource());
        sut = new AliasService(sourceCodeJapaneseReader, new OnMemoryAliasRepository());
    }

    @Test
    void クラス別名取得() {
        RawSource source = getTestRawSource();
        AliasSource aliasSource = source.textSource();

        sut.loadAliases(aliasSource);

        Assertions.assertThat(sut.typeAliasOf(new TypeIdentifier(KotlinStub.class)).asText())
                .isEqualTo("KotlinのクラスのDoc");
    }


    @Test
    void Kotlinメソッドの和名取得() {
        RawSource source = getTestRawSource();
        AliasSource aliasSource = source.textSource();

        sut.loadAliases(aliasSource);

        MethodIdentifier methodIdentifier = new MethodIdentifier(new TypeIdentifier(KotlinMethodJavadocStub.class), new MethodSignature(
                "simpleMethod",
                new org.dddjava.jig.domain.model.implementation.analyzed.declaration.method.Arguments(Collections.emptyList())));
        Assertions.assertThat(sut.methodAliasOf(methodIdentifier).asText())
                .isEqualTo("メソッドのドキュメント");

        MethodIdentifier overloadMethodIdentifier1 = new MethodIdentifier(new TypeIdentifier(KotlinMethodJavadocStub.class), new MethodSignature(
                "overloadMethod",
                new org.dddjava.jig.domain.model.implementation.analyzed.declaration.method.Arguments(Collections.emptyList())));
        Assertions.assertThat(sut.methodAliasOf(overloadMethodIdentifier1).asText())
                // オーバーロードは一意にならないのでどちらか
                .matches("引数(なし|あり)のメソッド");

        MethodIdentifier overloadMethodIdentifier2 = new MethodIdentifier(new TypeIdentifier(KotlinMethodJavadocStub.class), new MethodSignature(
                "overloadMethod",
                new org.dddjava.jig.domain.model.implementation.analyzed.declaration.method.Arguments(Arrays.asList(new TypeIdentifier(String.class), new TypeIdentifier(LocalDateTime.class)))));
        Assertions.assertThat(sut.methodAliasOf(overloadMethodIdentifier2).asText())
                // オーバーロードは一意にならないのでどちらか
                .matches("引数(なし|あり)のメソッド");
    }

    public RawSource getTestRawSource() {
        RawSourceLocations rawSourceLocations = getRawSourceLocations();
        LocalFileRawSourceFactory localFileRawSourceFactory = new LocalFileRawSourceFactory();
        return localFileRawSourceFactory.createSource(rawSourceLocations);
    }

    public RawSourceLocations getRawSourceLocations() {
        return new RawSourceLocations(
                new BinarySourceLocations(Collections.singletonList(Paths.get(defaultPackageClassURI()))),
                new TextSourceLocations(Collections.singletonList(getModuleRootPath().resolve("src").resolve("test").resolve("java")))
        );
    }

    static Path getModuleRootPath() {
        URI uri = defaultPackageClassURI();
        Path path = Paths.get(uri).toAbsolutePath();

        while (!path.endsWith("jig-cli-kt")) {
            path = path.getParent();
            if (path == null) {
                throw new IllegalStateException("モジュール名変わった？");
            }
        }
        return path;
    }

    static URI defaultPackageClassURI() {
        try {
            return AliasServiceTest.class.getResource("/DefaultPackageClass.class").toURI().resolve("./");
        } catch (URISyntaxException e) {
            throw new AssertionError(e);
        }
    }
}
