package org.dddjava.jig.application.service;

import org.assertj.core.api.Assertions;
import org.dddjava.jig.domain.model.businessrules.BusinessRuleCondition;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.method.MethodIdentifier;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.method.MethodSignature;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.raw.raw.BinarySourceLocations;
import org.dddjava.jig.domain.model.implementation.raw.raw.RawSource;
import org.dddjava.jig.domain.model.implementation.raw.raw.RawSourceLocations;
import org.dddjava.jig.domain.model.implementation.raw.raw.TextSourceLocations;
import org.dddjava.jig.domain.model.implementation.raw.textfile.TextSource;
import org.dddjava.jig.infrastructure.LocalFileRawSourceFactory;
import org.dddjava.jig.infrastructure.codeparser.SourceCodeJapaneseReader;
import org.dddjava.jig.infrastructure.configuration.Configuration;
import org.dddjava.jig.infrastructure.configuration.JigProperties;
import org.dddjava.jig.infrastructure.configuration.OutputOmitPrefix;
import org.dddjava.jig.infrastructure.javaparser.JavaparserAliasReader;
import org.dddjava.jig.infrastructure.kotlin.KotlinparserJapaneseReader;
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

public class GlossaryServiceTest {

    GlossaryService sut;

    GlossaryServiceTest() {
        SourceCodeJapaneseReader sourceCodeJapaneseReader = new SourceCodeJapaneseReader(Arrays.asList(new JavaparserAliasReader(), new KotlinparserJapaneseReader()));
        Configuration configuration = new Configuration(
                new JigProperties(
                        new BusinessRuleCondition("stub.domain.model.+"),
                        new OutputOmitPrefix()
                ),
                sourceCodeJapaneseReader
        );

        sut = new GlossaryService(sourceCodeJapaneseReader, new OnMemoryAliasRepository());
    }

    @Test
    void クラス別名取得() {
        RawSource source = getTestRawSource();
        TextSource textSource = source.textSource();

        sut.loadAliases(textSource.javaSources());
        sut.loadAliases(textSource.kotlinSources());

        Assertions.assertThat(sut.typeAliasOf(new TypeIdentifier(KotlinStub.class)).asText())
                .isEqualTo("KotlinのクラスのDoc");
    }


    @Test
    void Kotlinメソッドの和名取得() {
        RawSource source = getTestRawSource();
        TextSource textSource = source.textSource();

        sut.loadAliases(textSource.kotlinSources());

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
            return GlossaryServiceTest.class.getResource("/DefaultPackageClass.class").toURI().resolve("./");
        } catch (URISyntaxException e) {
            throw new AssertionError(e);
        }
    }
}
