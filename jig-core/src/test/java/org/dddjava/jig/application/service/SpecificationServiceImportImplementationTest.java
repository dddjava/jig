package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.ProjectData;
import org.dddjava.jig.domain.model.implementation.bytecode.ImplementationSources;
import org.dddjava.jig.domain.model.values.ValueKind;
import org.dddjava.jig.infrastructure.LocalProject;
import org.dddjava.jig.infrastructure.PropertyImplementationAnalyzeContext;
import org.dddjava.jig.infrastructure.asm.AsmImplementationFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import stub.domain.model.type.*;
import stub.domain.model.type.fuga.FugaIdentifier;
import stub.domain.model.type.fuga.FugaName;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class SpecificationServiceImportImplementationTest {

    private static ProjectData projectData;

    @BeforeAll
    static void before() throws URISyntaxException {
        // 読み込む対象のソースを取得
        URI location = SpecificationServiceImportImplementationTest.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        Path value = Paths.get(location);
        LocalProject localProject = new LocalProject(value.toString(), value.toString(), "not/read/resources", "not/read/sources");
        ImplementationSources implementationSources = localProject.getSpecificationSources();

        SpecificationService specificationService = new SpecificationService(new AsmImplementationFactory(new PropertyImplementationAnalyzeContext()));
        projectData = specificationService.importSpecification(implementationSources, new ProjectData());
    }

    @Test
    void 識別子() {
        assertThat(projectData.valueTypes().extract(ValueKind.IDENTIFIER).list())
                .extracting(TypeIdentifier::fullQualifiedName)
                .containsExactly(
                        SimpleIdentifier.class.getTypeName(),
                        FugaIdentifier.class.getTypeName(),
                        FugaName.class.getTypeName()
                );
    }

    @Test
    void 数値() {
        assertThat(projectData.valueTypes().extract(ValueKind.NUMBER).list()).extracting(TypeIdentifier::fullQualifiedName)
                .containsExactly(SimpleNumber.class.getTypeName());
    }

    @Test
    void 日付() {
        assertThat(projectData.valueTypes().extract(ValueKind.DATE).list()).extracting(TypeIdentifier::fullQualifiedName)
                .containsExactly(SimpleDate.class.getTypeName());
    }

    @Test
    void 期間() {
        assertThat(projectData.valueTypes().extract(ValueKind.TERM).list()).extracting(TypeIdentifier::fullQualifiedName)
                .containsExactly(SimpleTerm.class.getTypeName());
    }

    @Test
    void コレクション() {
        assertThat(projectData.valueTypes().extract(ValueKind.COLLECTION).list()).extracting(TypeIdentifier::fullQualifiedName)
                .containsExactlyInAnyOrder(
                        SimpleCollection.class.getTypeName(),
                        SetCollection.class.getTypeName());
    }

}
