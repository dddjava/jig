package jig.infrastructure.asm;

import jig.domain.model.characteristic.Characteristic;
import jig.domain.model.characteristic.CharacteristicRepository;
import jig.domain.model.identifier.MethodIdentifier;
import jig.domain.model.identifier.MethodIdentifiers;
import jig.domain.model.identifier.TypeIdentifier;
import jig.domain.model.identifier.TypeIdentifiers;
import jig.domain.model.project.ProjectLocation;
import jig.domain.model.relation.RelationRepository;
import jig.infrastructure.JigPaths;
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

public class AsmClassFileReaderTest {

    static CharacteristicRepository characteristicRepository = new OnMemoryCharacteristicRepository();
    static RelationRepository relationRepository = new OnMemoryRelationRepository();

    @BeforeAll
    static void before() throws URISyntaxException {

        URI location = AsmClassFileReaderTest.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        Path path = Paths.get(location);

        JigPaths jigPaths = new JigPaths(path.toString(), path.toString(), path.toString());
        AsmClassFileReader analyzer = new AsmClassFileReader(characteristicRepository, relationRepository, jigPaths);

        analyzer.readFrom(new ProjectLocation(path));
    }

    @Test
    void 関連() {
        MethodIdentifiers methods = relationRepository.findConcrete(new MethodIdentifier(
                new TypeIdentifier(HogeRepository.class),
                "method",
                new TypeIdentifiers(Collections.emptyList())));
        assertThat(methods.list()).isNotEmpty();
    }

    @Test
    void サービス() {
        assertThat(characteristicRepository.find(Characteristic.SERVICE).list()).extracting(TypeIdentifier::value)
                .containsExactly(CanonicalService.class.getTypeName());
    }

    @Test
    void 区分() {
        assertThat(characteristicRepository.find(Characteristic.ENUM).list()).hasSize(5);

        assertThat(characteristicRepository.find(Characteristic.ENUM_BEHAVIOUR).list()).extracting(TypeIdentifier::value)
                .containsExactly(BehaviourEnum.class.getTypeName(), RichEnum.class.getTypeName());
        assertThat(characteristicRepository.find(Characteristic.ENUM_PARAMETERIZED).list()).extracting(TypeIdentifier::value)
                .containsExactly(ParameterizedEnum.class.getTypeName(), RichEnum.class.getTypeName());
        assertThat(characteristicRepository.find(Characteristic.ENUM_POLYMORPHISM).list()).extracting(TypeIdentifier::value)
                .containsExactly(PolymorphismEnum.class.getTypeName(), RichEnum.class.getTypeName());
    }

    @Test
    void 識別子() {
        assertThat(characteristicRepository.find(Characteristic.IDENTIFIER).list()).extracting(TypeIdentifier::value)
                .containsExactly(SimpleIdentifier.class.getTypeName());
    }

    @Test
    void 数値() {
        assertThat(characteristicRepository.find(Characteristic.NUMBER).list()).extracting(TypeIdentifier::value)
                .containsExactly(SimpleNumber.class.getTypeName());
    }

    @Test
    void 日付() {

        assertThat(characteristicRepository.find(Characteristic.DATE).list()).extracting(TypeIdentifier::value)
                .containsExactly(SimpleDate.class.getTypeName());
    }

    @Test
    void 期間() {

        assertThat(characteristicRepository.find(Characteristic.TERM).list()).extracting(TypeIdentifier::value)
                .containsExactly(SimpleTerm.class.getTypeName());
    }

    @Test
    void コレクション() {
        assertThat(characteristicRepository.find(Characteristic.COLLECTION).list()).extracting(TypeIdentifier::value)
                .containsExactly(SimpleCollection.class.getTypeName());
    }

}
