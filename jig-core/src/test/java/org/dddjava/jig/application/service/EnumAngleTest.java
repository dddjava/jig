package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.categories.CategoryAngle;
import org.dddjava.jig.domain.model.categories.CategoryAngles;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.ProjectData;
import org.dddjava.jig.infrastructure.Layout;
import org.dddjava.jig.infrastructure.LocalProject;
import org.dddjava.jig.infrastructure.PropertyCharacterizedTypeFactory;
import org.dddjava.jig.infrastructure.asm.AsmByteCodeFactory;
import org.dddjava.jig.infrastructure.javaparser.JavaparserJapaneseReader;
import org.dddjava.jig.infrastructure.mybatis.MyBatisSqlReader;
import org.dddjava.jig.infrastructure.onmemoryrepository.OnMemoryJapaneseNameRepository;
import org.junit.jupiter.api.Test;
import testing.TestSupport;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EnumAngleTest {

    // テストのためにSpringを起動したくないので自分でインスタンス生成する
    ImplementationService implementationService = new ImplementationService(
            new AsmByteCodeFactory(),
            new GlossaryService(
                    new JavaparserJapaneseReader(),
                    new OnMemoryJapaneseNameRepository()
            ),
            new MyBatisSqlReader(),
            new PropertyCharacterizedTypeFactory()
    );

    AngleService angleService = new AngleService();

    @Test
    void readProjectData() {
        Layout layoutMock = mock(Layout.class);
        when(layoutMock.extractClassPath()).thenReturn(new Path[]{Paths.get(TestSupport.defaultPackageClassURI())});
        when(layoutMock.extractSourcePath()).thenReturn(new Path[0]);

        LocalProject localProject = new LocalProject(layoutMock);
        ProjectData projectData = implementationService.readProjectData(localProject);

        CategoryAngles categoryAngles = angleService.enumAngles(projectData);
        assertThat(categoryAngles.list())
                .extracting(
                        CategoryAngle::typeIdentifier,
                        categoryAngle -> categoryAngle.constantsDeclarationsName(),
                        categoryAngle -> categoryAngle.fieldDeclarations(),
                        categoryAngle -> categoryAngle.userTypeIdentifiers().asSimpleText(),
                        categoryAngle -> categoryAngle.hasParameter(),
                        categoryAngle -> categoryAngle.hasBehaviour(),
                        categoryAngle -> categoryAngle.isPolymorphism()
                ).contains(
                tuple(
                        new TypeIdentifier("stub.domain.model.kind.BehaviourEnum"),
                        "[A, B]", "[]", "[AsmByteCodeReaderTest, RelationEnum]",
                        false, true, false
                ),
                tuple(
                        new TypeIdentifier("stub.domain.model.kind.ParameterizedEnum"),
                        "[A, B]", "[String param]", "[AsmByteCodeReaderTest, RelationEnum]",
                        true, false, false
                ),
                tuple(
                        new TypeIdentifier("stub.domain.model.kind.PolymorphismEnum"),
                        "[A, B]", "[]", "[AsmByteCodeReaderTest, RelationEnum]",
                        false, false, true
                ),
                tuple(
                        new TypeIdentifier("stub.domain.model.kind.RelationEnum"),
                        "[A, B, C]", "[RichEnum field]", "[]",
                        true, false, false
                ),
                tuple(
                        new TypeIdentifier("stub.domain.model.kind.RichEnum"),
                        "[A, B]", "[String param]", "[AsmByteCodeReaderTest, RelationEnum]",
                        true, true, true
                ),
                tuple(
                        new TypeIdentifier("stub.domain.model.kind.SimpleEnum"),
                        "[A, B, C, D]", "[]", "[AsmByteCodeReaderTest, RelationEnum]",
                        false, false, false
                )
        );
    }
}
