package org.dddjava.jig.application.service;

import org.dddjava.jig.application.JigSourceReader;
import org.dddjava.jig.domain.model.models.jigobject.class_.JigType;
import org.dddjava.jig.domain.model.models.jigobject.member.JigMethod;
import org.dddjava.jig.domain.model.models.jigobject.member.JigMethods;
import org.dddjava.jig.domain.model.parts.classes.method.Arguments;
import org.dddjava.jig.domain.model.parts.classes.method.MethodSignature;
import org.dddjava.jig.domain.model.parts.classes.type.ClassComment;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.sources.file.SourcePaths;
import org.dddjava.jig.domain.model.sources.file.Sources;
import org.dddjava.jig.domain.model.sources.file.binary.BinarySourcePaths;
import org.dddjava.jig.domain.model.sources.file.text.CodeSourcePaths;
import org.dddjava.jig.domain.model.sources.jigfactory.TypeFacts;
import org.dddjava.jig.domain.model.sources.jigreader.AdditionalTextSourceReader;
import org.dddjava.jig.domain.model.sources.jigreader.TextSourceReader;
import org.dddjava.jig.infrastructure.asm.AsmFactReader;
import org.dddjava.jig.infrastructure.configuration.JigProperties;
import org.dddjava.jig.infrastructure.filesystem.LocalClassFileSourceReader;
import org.dddjava.jig.infrastructure.javaparser.JavaparserReader;
import org.dddjava.jig.infrastructure.kotlin.KotlinSdkReader;
import org.dddjava.jig.infrastructure.onmemoryrepository.OnMemoryCommentRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import stub.domain.model.KotlinMethodJavadocStub;
import stub.domain.model.KotlinStub;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CommentTest {

    JigSourceReader jigSourceReader;

    CommentTest() {
        jigSourceReader = new JigSourceReader(
                new OnMemoryCommentRepository(),
                new AsmFactReader(),
                new TextSourceReader(
                        new JavaparserReader(Mockito.mock(JigProperties.class)),
                        new AdditionalTextSourceReader(new KotlinSdkReader())),
                null,
                new LocalClassFileSourceReader());
    }

    @Test
    void クラス別名取得() {
        Sources source = getTestRawSource();
        TypeFacts typeFacts = jigSourceReader.readProjectData(source).typeFacts();
        ClassComment classComment = typeFacts.jigTypes().list()
                .stream().filter(jigType -> jigType.identifier().equals(new TypeIdentifier(KotlinStub.class)))
                .map(jigType -> jigType.typeAlias())
                .findAny().orElseThrow(AssertionError::new);

        assertEquals("KotlinのクラスのDoc", classComment.asText());
    }


    @Test
    void Kotlinメソッドの和名取得() {
        Sources source = getTestRawSource();
        TypeFacts typeFacts = jigSourceReader.readProjectData(source).typeFacts();

        TypeIdentifier テスト対象クラス = new TypeIdentifier(KotlinMethodJavadocStub.class);
        JigType jigType = typeFacts.jigTypes().listMatches(item -> item.identifier().equals(テスト対象クラス)).get(0);

        JigMethods jigMethods = jigType.instanceMember().instanceMethods();

        JigMethod method = jigMethods.resolveMethodBySignature(new MethodSignature("simpleMethod"));
        assertEquals("メソッドのドキュメント", method.aliasTextOrBlank());

        JigMethod overloadedMethod = jigMethods.resolveMethodBySignature(
                new MethodSignature("overloadMethod", new Arguments(Arrays.asList(new TypeIdentifier(String.class), new TypeIdentifier(LocalDateTime.class)))));
        assertTrue(overloadedMethod.aliasTextOrBlank().matches("引数(なし|あり)のメソッド"));

        JigMethod overloadedMethod2 = jigMethods.resolveMethodBySignature(new MethodSignature("overloadMethod"));
        assertTrue(overloadedMethod2.aliasTextOrBlank().matches("引数(なし|あり)のメソッド"));
    }

    public Sources getTestRawSource() {
        SourcePaths sourcePaths = getRawSourceLocations();
        LocalClassFileSourceReader localFileRawSourceFactory = new LocalClassFileSourceReader();
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
            return CommentTest.class.getResource("/" + defaultPackageClass + ".class").toURI().resolve("./");
        } catch (URISyntaxException e) {
            throw new AssertionError(e);
        }
    }
}
