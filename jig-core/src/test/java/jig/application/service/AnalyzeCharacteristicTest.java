package jig.application.service;

import jig.domain.model.characteristic.Characteristic;
import jig.domain.model.characteristic.CharacteristicRepository;
import jig.domain.model.declaration.annotation.AnnotationDeclarationRepository;
import jig.domain.model.declaration.method.MethodDeclaration;
import jig.domain.model.declaration.method.MethodDeclarations;
import jig.domain.model.declaration.method.MethodSignature;
import jig.domain.model.identifier.type.TypeIdentifier;
import jig.domain.model.project.ProjectLocation;
import jig.domain.model.relation.RelationRepository;
import jig.domain.model.specification.SpecificationSources;
import jig.domain.model.specification.Specifications;
import jig.infrastructure.JigPaths;
import jig.infrastructure.PropertySpecificationContext;
import jig.infrastructure.asm.AsmSpecificationReader;
import jig.infrastructure.onmemoryrepository.OnMemoryAnnotationDeclarationRepository;
import jig.infrastructure.onmemoryrepository.OnMemoryCharacteristicRepository;
import jig.infrastructure.onmemoryrepository.OnMemoryRelationRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import stub.application.service.CanonicalService;
import stub.domain.model.kind.*;
import stub.domain.model.type.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class AnalyzeCharacteristicTest {

    static CharacteristicRepository characteristicRepository = new OnMemoryCharacteristicRepository();
    static RelationRepository relationRepository = new OnMemoryRelationRepository();
    static AnnotationDeclarationRepository annotationDeclarationRepository = new OnMemoryAnnotationDeclarationRepository();

    @BeforeAll
    static void before() throws URISyntaxException {
        // 読み込む対象のソースを取得
        URI location = AnalyzeCharacteristicTest.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        ProjectLocation projectLocation = new ProjectLocation(Paths.get(location));
        JigPaths jigPaths = new JigPaths(projectLocation.toPath().toString(), "not/read/resources", "not/read/sources");
        SpecificationSources specificationSources = jigPaths.getSpecificationSources(projectLocation);

        // 仕様化
        Specifications specifications = new AsmSpecificationReader(
                new PropertySpecificationContext()
        ).readFrom(specificationSources);

        // 仕様から特徴と関連を登録
        DependencyService dependencyService = new DependencyService(characteristicRepository, relationRepository, annotationDeclarationRepository);
        dependencyService.registerSpecifications(specifications);
    }

    @Test
    void 関連() {
        MethodDeclarations methods = relationRepository.findConcrete(new MethodDeclaration(
                new TypeIdentifier(HogeRepository.class),
                new MethodSignature("method", Collections.emptyList())));
        assertThat(methods.list()).isNotEmpty();
    }

    @Test
    void サービス() {
        assertThat(characteristicRepository.getTypeIdentifiersOf(Characteristic.SERVICE).list()).extracting(TypeIdentifier::fullQualifiedName)
                .containsExactly(CanonicalService.class.getTypeName());
    }

    @Test
    void 区分() {
        assertThat(characteristicRepository.getTypeIdentifiersOf(Characteristic.ENUM_BEHAVIOUR).list()).extracting(TypeIdentifier::fullQualifiedName)
                .contains(BehaviourEnum.class.getTypeName(), RichEnum.class.getTypeName())
                .doesNotContain(ParameterizedEnum.class.getTypeName(), PolymorphismEnum.class.getTypeName(), SimpleEnum.class.getTypeName());
        assertThat(characteristicRepository.getTypeIdentifiersOf(Characteristic.ENUM_PARAMETERIZED).list()).extracting(TypeIdentifier::fullQualifiedName)
                .contains(ParameterizedEnum.class.getTypeName(), RichEnum.class.getTypeName())
                .doesNotContain(BehaviourEnum.class.getTypeName(), PolymorphismEnum.class.getTypeName(), SimpleEnum.class.getTypeName());
        assertThat(characteristicRepository.getTypeIdentifiersOf(Characteristic.ENUM_POLYMORPHISM).list()).extracting(TypeIdentifier::fullQualifiedName)
                .contains(PolymorphismEnum.class.getTypeName(), RichEnum.class.getTypeName())
                .doesNotContain(BehaviourEnum.class.getTypeName(), ParameterizedEnum.class.getTypeName(), SimpleEnum.class.getTypeName());
    }

    @Test
    void 識別子() {
        assertThat(characteristicRepository.getTypeIdentifiersOf(Characteristic.IDENTIFIER).list()).extracting(TypeIdentifier::fullQualifiedName)
                .containsExactly(SimpleIdentifier.class.getTypeName());
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
                .containsExactly(SimpleCollection.class.getTypeName());
    }

}
