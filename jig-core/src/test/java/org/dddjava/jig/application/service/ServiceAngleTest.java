package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.ProjectData;
import org.dddjava.jig.domain.model.services.ServiceAngle;
import org.dddjava.jig.domain.model.services.ServiceAngles;
import org.dddjava.jig.infrastructure.Layout;
import org.dddjava.jig.infrastructure.LocalProject;
import org.dddjava.jig.infrastructure.PropertyCharacterizedTypeFactory;
import org.dddjava.jig.infrastructure.asm.AsmByteCodeFactory;
import org.dddjava.jig.infrastructure.configuration.RepositoryPattern;
import org.dddjava.jig.infrastructure.javaparser.JavaparserJapaneseReader;
import org.dddjava.jig.infrastructure.mybatis.MyBatisSqlReader;
import org.dddjava.jig.infrastructure.onmemoryrepository.OnMemoryJapaneseNameRepository;
import org.junit.jupiter.api.Test;
import stub.application.service.CanonicalService;
import stub.application.service.DecisionService;
import stub.application.service.SimpleService;
import stub.domain.model.type.fuga.Fuga;
import testing.TestSupport;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ServiceAngleTest {

    // テストのためにSpringを起動したくないので自分でインスタンス生成する
    ImplementationService implementationService = new ImplementationService(
            new AsmByteCodeFactory(),
            new GlossaryService(
                    new JavaparserJapaneseReader(),
                    new OnMemoryJapaneseNameRepository()
            ),
            new MyBatisSqlReader(),
            new PropertyCharacterizedTypeFactory(new RepositoryPattern())
    );

    ApplicationService applicationService = new ApplicationService();

    @Test
    void readProjectData() {
        Layout layoutMock = mock(Layout.class);
        when(layoutMock.extractClassPath()).thenReturn(new Path[]{Paths.get(TestSupport.defaultPackageClassURI())});
        when(layoutMock.extractSourcePath()).thenReturn(new Path[0]);

        LocalProject localProject = new LocalProject(layoutMock);
        ProjectData projectData = implementationService.readProjectData(localProject);

        ServiceAngles serviceAngles = applicationService.serviceAngles(projectData);

        assertThat(serviceAngles.list())
                .extracting(
                        serviceAngle -> serviceAngle.method().declaringType(),
                        serviceAngle -> serviceAngle.method().asSignatureSimpleText(),
                        serviceAngle -> serviceAngle.method().returnType(),
                        ServiceAngle::usingFromController,
                        serviceAngle -> serviceAngle.usingRepositoryMethods()
                ).contains(
                tuple(
                        new TypeIdentifier(CanonicalService.class),
                        "fuga(FugaIdentifier)",
                        new TypeIdentifier(Fuga.class),
                        false,
                        "[FugaRepository.get(FugaIdentifier), HogeRepository.method()]"
                ),
                tuple(
                        new TypeIdentifier(DecisionService.class),
                        "分岐のあるメソッド(Object)",
                        new TypeIdentifier("void"),
                        false,
                        "[]"
                ),
                tuple(
                        new TypeIdentifier(SimpleService.class),
                        "RESTコントローラーから呼ばれる()",
                        new TypeIdentifier("void"),
                        true,
                        "[]"
                ),
                tuple(
                        new TypeIdentifier(SimpleService.class),
                        "コントローラーから呼ばれない()",
                        new TypeIdentifier("void"),
                        false,
                        "[]"
                ),
                tuple(
                        new TypeIdentifier(SimpleService.class),
                        "コントローラーから呼ばれる()",
                        new TypeIdentifier("void"),
                        true,
                        "[]"
                )
        );
    }
}
