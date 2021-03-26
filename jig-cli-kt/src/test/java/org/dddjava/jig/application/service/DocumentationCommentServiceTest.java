package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.jigmodel.jigtype.member.JigMethod;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.alias.TypeAlias;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.method.Arguments;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.method.MethodIdentifier;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.method.MethodSignature;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.jigsource.file.SourcePaths;
import org.dddjava.jig.domain.model.jigsource.file.Sources;
import org.dddjava.jig.domain.model.jigsource.file.binary.BinarySourcePaths;
import org.dddjava.jig.domain.model.jigsource.file.text.CodeSourcePaths;
import org.dddjava.jig.domain.model.jigsource.jigloader.SourceCodeAliasReader;
import org.dddjava.jig.domain.model.jigsource.jigloader.analyzed.MethodFact;
import org.dddjava.jig.domain.model.jigsource.jigloader.analyzed.TypeFacts;
import org.dddjava.jig.infrastructure.asm.AsmFactReader;
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
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DocumentationCommentServiceTest {

    JigSourceReadService jigSourceReadService;
    AliasService sut;

    DocumentationCommentServiceTest() {
        SourceCodeAliasReader sourceCodeAliasReader = new SourceCodeAliasReader(new JavaparserAliasReader(), new KotlinSdkAliasReader());
        OnMemoryAliasRepository onMemoryAliasRepository = new OnMemoryAliasRepository();
        OnMemoryJigSourceRepository jigSourceRepository = new OnMemoryJigSourceRepository(onMemoryAliasRepository);
        jigSourceReadService = new JigSourceReadService(jigSourceRepository, new AsmFactReader(), sourceCodeAliasReader, null, new LocalFileSourceReader());
        sut = new AliasService(onMemoryAliasRepository);
    }

    @Test
    void クラス別名取得() {
        Sources source = getTestRawSource();
        TypeFacts typeFacts = jigSourceReadService.readProjectData(source);
        TypeAlias typeAlias = typeFacts.listJigTypes()
                .stream().filter(jigType -> jigType.identifier().equals(new TypeIdentifier(KotlinStub.class)))
                .map(jigType -> jigType.typeAlias())
                .findAny().orElseThrow(AssertionError::new);

        assertEquals("KotlinのクラスのDoc", typeAlias.asText());
    }


    @Test
    void Kotlinメソッドの和名取得() {
        Sources source = getTestRawSource();
        TypeFacts typeFacts = jigSourceReadService.readProjectData(source);
        List<JigMethod> methods = typeFacts.instanceMethodFacts().stream().map(MethodFact::createMethod).collect(Collectors.toList());

        JigMethod simpleMethod = methods.stream()
                .filter(e -> e.declaration().identifier().equals(new MethodIdentifier(
                        new TypeIdentifier(KotlinMethodJavadocStub.class),
                        new MethodSignature("simpleMethod"))))
                .findAny().orElseThrow(AssertionError::new);
        assertEquals("メソッドのドキュメント", simpleMethod.aliasTextOrBlank());

        JigMethod overloadMethod1 = methods.stream()
                .filter(e -> e.declaration().identifier().equals(new MethodIdentifier(
                        new TypeIdentifier(KotlinMethodJavadocStub.class),
                        new MethodSignature("overloadMethod"))))
                .findAny().orElseThrow(AssertionError::new);
        assertTrue(overloadMethod1.aliasTextOrBlank().matches("引数(なし|あり)のメソッド"));

        JigMethod overloadMethod2 = methods.stream()
                .filter(e -> e.declaration().identifier().equals(new MethodIdentifier(
                        new TypeIdentifier(KotlinMethodJavadocStub.class),
                        new MethodSignature("overloadMethod", new Arguments(Arrays.asList(new TypeIdentifier(String.class), new TypeIdentifier(LocalDateTime.class)))))))
                .findAny().orElseThrow(AssertionError::new);
        assertTrue(overloadMethod2.aliasTextOrBlank().matches("引数(なし|あり)のメソッド"));
    }

    public Sources getTestRawSource() {
        SourcePaths sourcePaths = getRawSourceLocations();
        LocalFileSourceReader localFileRawSourceFactory = new LocalFileSourceReader();
        return localFileRawSourceFactory.readSources(sourcePaths);
    }

    public SourcePaths getRawSourceLocations() {
        return new SourcePaths(
                new BinarySourcePaths(Arrays.asList(
                        Paths.get(defaultPackageClassURI("DefaultPackageKtClass")),
                        Paths.get(defaultPackageClassURI("DefaultPackageClass"))
                )),
                new CodeSourcePaths(Collections.singletonList(getModuleRootPath().resolve("src").resolve("test").resolve("kotlin")))
        );
    }

    static Path getModuleRootPath() {
        URI uri = defaultPackageClassURI("DefaultPackageClass");
        Path path = Paths.get(uri).toAbsolutePath();

        while (!path.endsWith("jig-cli-kt")) {
            path = path.getParent();
            if (path == null) {
                throw new IllegalStateException("モジュール名変わった？");
            }
        }
        return path;
    }

    static URI defaultPackageClassURI(String defaultPackageClass) {
        try {
            return DocumentationCommentServiceTest.class.getResource("/" + defaultPackageClass + ".class").toURI().resolve("./");
        } catch (URISyntaxException e) {
            throw new AssertionError(e);
        }
    }
}
