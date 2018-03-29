package jig.application.service;

import jig.domain.model.characteristic.Characteristic;
import jig.domain.model.characteristic.CharacteristicRepository;
import jig.domain.model.identifier.method.MethodIdentifier;
import jig.domain.model.identifier.method.MethodIdentifiers;
import jig.domain.model.identifier.method.MethodSignature;
import jig.domain.model.identifier.type.TypeIdentifier;
import jig.domain.model.project.ProjectLocation;
import jig.domain.model.relation.RelationRepository;
import jig.infrastructure.JigPaths;
import jig.infrastructure.asm.AsmClassFileReader;
import jig.infrastructure.onmemoryrepository.OnMemoryCharacteristicRepository;
import jig.infrastructure.onmemoryrepository.OnMemoryRelationRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import stub.application.service.CanonicalService;
import stub.domain.model.kind.BehaviourEnum;
import stub.domain.model.kind.ParameterizedEnum;
import stub.domain.model.kind.PolymorphismEnum;
import stub.domain.model.kind.RichEnum;
import stub.domain.model.type.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class AnalyzeCharacteristicTest {

    static CharacteristicRepository characteristicRepository = new OnMemoryCharacteristicRepository();
    static RelationRepository relationRepository = new OnMemoryRelationRepository();

    @BeforeAll
    static void before() throws URISyntaxException {
        URI location = AnalyzeCharacteristicTest.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        Path path = Paths.get(location);

        JigPaths jigPaths = new JigPaths(path.toString(), path.toString(), path.toString());

        DependencyService dependencyService = new DependencyService(characteristicRepository, relationRepository);

        new AnalyzeService(new AsmClassFileReader(), null, null, dependencyService, jigPaths, null)
                .importSpecification(new ProjectLocation(path));
    }

    @Test
    void 関連() {
        MethodIdentifiers methods = relationRepository.findConcrete(new MethodIdentifier(
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
        assertThat(characteristicRepository.getTypeIdentifiersOf(Characteristic.ENUM).list()).hasSize(5);

        assertThat(characteristicRepository.getTypeIdentifiersOf(Characteristic.ENUM_BEHAVIOUR).list()).extracting(TypeIdentifier::fullQualifiedName)
                .containsExactly(BehaviourEnum.class.getTypeName(), RichEnum.class.getTypeName());
        assertThat(characteristicRepository.getTypeIdentifiersOf(Characteristic.ENUM_PARAMETERIZED).list()).extracting(TypeIdentifier::fullQualifiedName)
                .containsExactly(ParameterizedEnum.class.getTypeName(), RichEnum.class.getTypeName());
        assertThat(characteristicRepository.getTypeIdentifiersOf(Characteristic.ENUM_POLYMORPHISM).list()).extracting(TypeIdentifier::fullQualifiedName)
                .containsExactly(PolymorphismEnum.class.getTypeName(), RichEnum.class.getTypeName());
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
