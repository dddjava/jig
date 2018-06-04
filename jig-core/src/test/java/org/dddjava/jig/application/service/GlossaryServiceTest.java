package org.dddjava.jig.application.service;

import org.assertj.core.api.Assertions;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodSignature;
import org.dddjava.jig.domain.model.identifier.namespace.PackageIdentifier;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;
import org.dddjava.jig.infrastructure.DefaultLocalProject;
import org.dddjava.jig.infrastructure.javaparser.JavaparserJapaneseReader;
import org.dddjava.jig.infrastructure.onmemoryrepository.OnMemoryJapaneseNameRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import stub.domain.model.ClassJavadocStub;
import stub.domain.model.MethodJavadocStub;
import stub.domain.model.NotJavadocStub;
import testing.TestSupport;

import java.util.Collections;
import java.util.stream.Stream;

class GlossaryServiceTest {

    GlossaryService sut = new GlossaryService(
            new JavaparserJapaneseReader(),
            new OnMemoryJapaneseNameRepository());

    @Test
    void パッケージ和名取得() {
        DefaultLocalProject localProject = new DefaultLocalProject(TestSupport.getModuleRootPath().toString(), "dummy", "dummy", "src/test/java");

        sut.importJapanese(localProject.getPackageNameSources());

        Assertions.assertThat(sut.japaneseNameFrom(new PackageIdentifier("stub")).value())
                .isEqualTo("テストで使用するスタブたち");
    }

    @ParameterizedTest
    @MethodSource
    void クラス和名取得(TypeIdentifier typeIdentifier, String comment) {
        DefaultLocalProject localProject = new DefaultLocalProject(TestSupport.getModuleRootPath().toString(), "dummy", "dummy", "src/test/java");

        sut.importJapanese(localProject.getTypeNameSources());

        Assertions.assertThat(sut.japaneseNameFrom(typeIdentifier).value())
                .isEqualTo(comment);
    }

    static Stream<Arguments> クラス和名取得() {
        return Stream.of(
                Arguments.of(new TypeIdentifier(ClassJavadocStub.class), "クラスのJavadoc"),
                Arguments.of(new TypeIdentifier(MethodJavadocStub.class), ""),
                Arguments.of(new TypeIdentifier(NotJavadocStub.class), ""),
                Arguments.of(new TypeIdentifier("DefaultPackageClass"), "デフォルトパッケージにあるクラス")
        );
    }

    @Test
    void メソッド和名取得() {
        DefaultLocalProject localProject = new DefaultLocalProject(TestSupport.getModuleRootPath().toString(), "dummy", "dummy", "src/test/java");

        sut.importJapanese(localProject.getTypeNameSources());

        MethodDeclaration methodDeclaration = new MethodDeclaration(
                new TypeIdentifier(MethodJavadocStub.class),
                new MethodSignature(
                        "method",
                        new org.dddjava.jig.domain.model.declaration.method.Arguments(Collections.emptyList())),
                new TypeIdentifier("void")
        );
        Assertions.assertThat(sut.japaneseNameFrom(methodDeclaration).value())
                .isEqualTo("メソッドのJavadoc");

        MethodDeclaration overloadMethodDeclaration = new MethodDeclaration(
                new TypeIdentifier(MethodJavadocStub.class),
                new MethodSignature(
                        "overloadMethod",
                        new org.dddjava.jig.domain.model.declaration.method.Arguments(Collections.singletonList(new TypeIdentifier(String.class)))),
                new TypeIdentifier("void")
        );
        Assertions.assertThat(sut.japaneseNameFrom(overloadMethodDeclaration).value())
                .isEqualTo("引数ありのメソッド");
    }
}
