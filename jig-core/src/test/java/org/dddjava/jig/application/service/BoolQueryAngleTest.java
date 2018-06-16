package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.booleans.model.BoolQueryAngles;
import org.dddjava.jig.domain.model.declaration.method.Arguments;
import org.dddjava.jig.domain.model.declaration.method.MethodIdentifier;
import org.dddjava.jig.domain.model.declaration.method.MethodSignature;
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
import stub.domain.model.booleans.*;
import testing.TestSupport;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BoolQueryAngleTest {

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

        BoolQueryAngles boolQueryAngles = angleService.boolQueryModelMethodAngle(projectData);

        assertThat(boolQueryAngles.list())
                .extracting(
                        boolQueryAngle -> boolQueryAngle.method().identifier(),
                        boolQueryAngle -> boolQueryAngle.userTypeIdentifiers().list()
                )
                .containsExactlyInAnyOrder(
                        tuple(methodOf("真偽値を返すメソッド"), Collections.emptyList()),
                        tuple(methodOf("呼び出される真偽値を返すメソッド"), Arrays.asList(
                                new TypeIdentifier(UserByConstructor.class),
                                new TypeIdentifier(UserByField.class),
                                new TypeIdentifier(UserByLambda.class),
                                new TypeIdentifier(UserByMethod.class),
                                new TypeIdentifier(UserByStaticField.class),
                                new TypeIdentifier(UserByStaticMethod.class)
                        ))
                )
                .doesNotContain(
                        tuple(methodOf("真偽値を返すstaticメソッド"), Collections.emptyList())
                );

    }

    private MethodIdentifier methodOf(String methodName) {
        return new MethodIdentifier(new TypeIdentifier(BooleanQueryAngleTestTarget.class), new MethodSignature(methodName, new Arguments(Collections.emptyList())));
    }
}
