package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.characteristic.Characteristic;
import org.dddjava.jig.domain.model.characteristic.CharacteristicRepository;
import org.dddjava.jig.domain.model.characteristic.CharacterizedMethodRepository;
import org.dddjava.jig.domain.model.declaration.annotation.AnnotationDeclarationRepository;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.declaration.method.MethodSignature;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.relation.RelationRepository;
import org.dddjava.jig.domain.model.implementation.ImplementationSources;
import org.dddjava.jig.infrastructure.LocalProject;
import org.dddjava.jig.infrastructure.PropertyImplementationAnalyzeContext;
import org.dddjava.jig.infrastructure.asm.AsmImplementationFactory;
import org.dddjava.jig.infrastructure.onmemoryrepository.OnMemoryAnnotationDeclarationRepository;
import org.dddjava.jig.infrastructure.onmemoryrepository.OnMemoryCharacteristicRepository;
import org.dddjava.jig.infrastructure.onmemoryrepository.OnMemoryCharacterizedMethodRepository;
import org.dddjava.jig.infrastructure.onmemoryrepository.OnMemoryRelationRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import stub.domain.model.type.*;
import stub.domain.model.type.fuga.FugaIdentifier;
import stub.domain.model.type.fuga.FugaName;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class SpecificationServiceImportImplementationTest {

    static CharacteristicRepository characteristicRepository = new OnMemoryCharacteristicRepository();
    static CharacterizedMethodRepository characterizedMethodRepository = new OnMemoryCharacterizedMethodRepository();

    static RelationRepository relationRepository = new OnMemoryRelationRepository();
    static AnnotationDeclarationRepository annotationDeclarationRepository = new OnMemoryAnnotationDeclarationRepository();

    @BeforeAll
    static void before() throws URISyntaxException {
        // 読み込む対象のソースを取得
        URI location = SpecificationServiceImportImplementationTest.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        Path value = Paths.get(location);
        LocalProject localProject = new LocalProject(value.toString(), value.toString(), "not/read/resources", "not/read/sources");
        ImplementationSources implementationSources = localProject.getSpecificationSources();

        SpecificationService specificationService = new SpecificationService(
                new AsmImplementationFactory(new PropertyImplementationAnalyzeContext()),
                new CharacteristicService(characteristicRepository, characterizedMethodRepository),
                relationRepository, annotationDeclarationRepository,
                mock(DependencyService.class));
        specificationService.importSpecification(implementationSources);
    }

    @Test
    void 関連() {
        MethodDeclarations methods = relationRepository.findConcrete(new MethodDeclaration(
                new TypeIdentifier(HogeRepository.class),
                new MethodSignature("method", Collections.emptyList()),
                new TypeIdentifier("void")));
        assertThat(methods.list()).isNotEmpty();
    }

    @Test
    void 識別子() {
        assertThat(characteristicRepository.getTypeIdentifiersOf(Characteristic.IDENTIFIER).list())
                .extracting(TypeIdentifier::fullQualifiedName)
                .containsExactly(
                        SimpleIdentifier.class.getTypeName(),
                        FugaIdentifier.class.getTypeName(),
                        FugaName.class.getTypeName()
                );
    }

    @Test
    void 数値() {
        assertThat(characteristicRepository.getTypeIdentifiersOf(Characteristic.NUMBER).list()).extracting(TypeIdentifier::fullQualifiedName)
                .containsExactly(SimpleNumber.class.getTypeName());
    }

    @Test
    void 日付() {
        assertThat(characteristicRepository.getTypeIdentifiersOf(Characteristic.DATE).list()).extracting(TypeIdentifier::fullQualifiedName)
                .containsExactly(SimpleDate.class.getTypeName());
    }

    @Test
    void 期間() {
        assertThat(characteristicRepository.getTypeIdentifiersOf(Characteristic.TERM).list()).extracting(TypeIdentifier::fullQualifiedName)
                .containsExactly(SimpleTerm.class.getTypeName());
    }

    @Test
    void コレクション() {
        assertThat(characteristicRepository.getTypeIdentifiersOf(Characteristic.COLLECTION).list()).extracting(TypeIdentifier::fullQualifiedName)
                .containsExactlyInAnyOrder(
                        SimpleCollection.class.getTypeName(),
                        SetCollection.class.getTypeName());
    }

}
