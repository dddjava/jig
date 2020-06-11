package org.dddjava.jig.application.service;

import org.assertj.core.api.Assertions;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.method.Arguments;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.method.MethodIdentifier;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.method.MethodSignature;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.jigsource.file.SourcePaths;
import org.dddjava.jig.domain.model.jigsource.file.Sources;
import org.dddjava.jig.domain.model.jigsource.file.binary.BinarySourcePaths;
import org.dddjava.jig.domain.model.jigsource.file.text.AliasSource;
import org.dddjava.jig.domain.model.jigsource.file.text.CodeSourcePaths;
import org.dddjava.jig.domain.model.jigsource.jigloader.SourceCodeAliasReader;
import org.dddjava.jig.infrastructure.filesystem.LocalFileSourceReader;
import org.dddjava.jig.infrastructure.javaparser.JavaparserAliasReader;
import org.dddjava.jig.infrastructure.kotlin.KotlinSdkAliasReader;
import org.dddjava.jig.infrastructure.onmemoryrepository.OnMemoryAliasRepository;
import org.dddjava.jig.infrastructure.onmemoryrepository.OnMemoryJigSourceRepository;
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

    JigSourceReadService jigSourceReadService;
    AliasService sut;

    AliasServiceTest() {
        SourceCodeAliasReader sourceCodeAliasReader = new SourceCodeAliasReader(new JavaparserAliasReader(), new KotlinSdkAliasReader());
        OnMemoryAliasRepository onMemoryAliasRepository = new OnMemoryAliasRepository();
        OnMemoryJigSourceRepository jigSourceRepository = new OnMemoryJigSourceRepository(onMemoryAliasRepository);
        jigSourceReadService = new JigSourceReadService(jigSourceRepository, null, sourceCodeAliasReader, null, null);
        sut = new AliasService(onMemoryAliasRepository);
    }

    @Test
    void クラス別名取得() {
        Sources source = getTestRawSource();
        AliasSource aliasSource = source.aliasSource();
        jigSourceReadService.readAliases(aliasSource);

        Assertions.assertThat(sut.typeAliasOf(new TypeIdentifier(KotlinStub.class)).asText())
                .isEqualTo("KotlinのクラスのDoc");
    }


    @Test
    void Kotlinメソッドの和名取得() {
        Sources source = getTestRawSource();
        AliasSource aliasSource = source.aliasSource();
        jigSourceReadService.readAliases(aliasSource);

        MethodIdentifier methodIdentifier = new MethodIdentifier(new TypeIdentifier(KotlinMethodJavadocStub.class), new MethodSignature(
                "simpleMethod",
                new Arguments(Collections.emptyList())));
        Assertions.assertThat(sut.methodAliasOf(methodIdentifier).asText())
                .isEqualTo("メソッドのドキュメント");

        MethodIdentifier overloadMethodIdentifier1 = new MethodIdentifier(new TypeIdentifier(KotlinMethodJavadocStub.class), new MethodSignature(
                "overloadMethod",
                new Arguments(Collections.emptyList())));
        Assertions.assertThat(sut.methodAliasOf(overloadMethodIdentifier1).asText())
                // オーバーロードは一意にならないのでどちらか
                .matches("引数(なし|あり)のメソッド");

        MethodIdentifier overloadMethodIdentifier2 = new MethodIdentifier(new TypeIdentifier(KotlinMethodJavadocStub.class), new MethodSignature(
                "overloadMethod",
                new Arguments(Arrays.asList(new TypeIdentifier(String.class), new TypeIdentifier(LocalDateTime.class)))));
        Assertions.assertThat(sut.methodAliasOf(overloadMethodIdentifier2).asText())
                // オーバーロードは一意にならないのでどちらか
                .matches("引数(なし|あり)のメソッド");
    }

    public Sources getTestRawSource() {
        SourcePaths sourcePaths = getRawSourceLocations();
        LocalFileSourceReader localFileRawSourceFactory = new LocalFileSourceReader();
        return localFileRawSourceFactory.readSources(sourcePaths);
    }

    public SourcePaths getRawSourceLocations() {
        return new SourcePaths(
                new BinarySourcePaths(Collections.singletonList(Paths.get(defaultPackageClassURI()))),
                new CodeSourcePaths(Collections.singletonList(getModuleRootPath().resolve("src").resolve("test").resolve("kotlin")))
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
