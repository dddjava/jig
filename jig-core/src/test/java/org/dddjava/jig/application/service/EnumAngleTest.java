package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.categories.CategoryAngle;
import org.dddjava.jig.domain.model.categories.CategoryAngles;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.ProjectData;
import org.dddjava.jig.infrastructure.Layout;
import org.dddjava.jig.infrastructure.LocalProject;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import testing.JigServiceTest;
import testing.TestSupport;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@JigServiceTest
public class EnumAngleTest {

    @Test
    void readProjectData(ImplementationService implementationService,  BusinessRuleService businessRuleService) {
        Logger logger = LoggerFactory.getLogger("");
        Map<String, Object> map = Collections.singletonMap("aaa", "bbb");
        logger.error("", map);


        Layout layoutMock = mock(Layout.class);
        when(layoutMock.extractClassPath()).thenReturn(new Path[]{Paths.get(TestSupport.defaultPackageClassURI())});
        when(layoutMock.extractSourcePath()).thenReturn(new Path[0]);

        LocalProject localProject = new LocalProject(layoutMock);
        ProjectData projectData = implementationService.readProjectData(localProject);

        CategoryAngles categoryAngles = businessRuleService.categories(projectData);
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
                        "[A, B]", "[]", "[AsmByteCodeFactoryTest, RelationEnum]",
                        false, true, false
                ),
                tuple(
                        new TypeIdentifier("stub.domain.model.kind.ParameterizedEnum"),
                        "[A, B]", "[String param]", "[AsmByteCodeFactoryTest, RelationEnum]",
                        true, false, false
                ),
                tuple(
                        new TypeIdentifier("stub.domain.model.kind.PolymorphismEnum"),
                        "[A, B]", "[]", "[AsmByteCodeFactoryTest, RelationEnum]",
                        false, false, true
                ),
                tuple(
                        new TypeIdentifier("stub.domain.model.kind.RelationEnum"),
                        "[A, B, C]", "[RichEnum field]", "[]",
                        true, false, false
                ),
                tuple(
                        new TypeIdentifier("stub.domain.model.kind.RichEnum"),
                        "[A, B]", "[String param]", "[AsmByteCodeFactoryTest, RelationEnum]",
                        true, true, true
                ),
                tuple(
                        new TypeIdentifier("stub.domain.model.kind.SimpleEnum"),
                        "[A, B, C, D]", "[]", "[AsmByteCodeFactoryTest, RelationEnum]",
                        false, false, false
                ),
                tuple(
                        new TypeIdentifier("stub.domain.model.kind.HasStaticFieldEnum"),
                        "[A, B]", "[]", "[]",
                        false, false, false
                )
        );
    }
}
