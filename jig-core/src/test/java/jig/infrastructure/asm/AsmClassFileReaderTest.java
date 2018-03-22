package jig.infrastructure.asm;

import jig.domain.model.characteristic.Characteristic;
import jig.domain.model.characteristic.CharacteristicRepository;
import jig.domain.model.identifier.Identifier;
import jig.domain.model.identifier.Identifiers;
import jig.domain.model.identifier.MethodIdentifier;
import jig.domain.model.identifier.MethodIdentifiers;
import jig.domain.model.relation.RelationRepository;
import jig.infrastructure.JigPaths;
import jig.infrastructure.RecursiveFileVisitor;
import jig.infrastructure.onmemoryrepository.OnMemoryCharacteristicRepository;
import jig.infrastructure.onmemoryrepository.OnMemoryRelationRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import stub.type.*;
import stub.type.kind.BehaviourEnum;
import stub.type.kind.ParameterizedEnum;
import stub.type.kind.PolymorphismEnum;
import stub.type.kind.RichEnum;

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

        AsmClassFileReader analyzer = new AsmClassFileReader(characteristicRepository, relationRepository, new JigPaths());
        RecursiveFileVisitor recursiveFileVisitor = new RecursiveFileVisitor(analyzer::execute);
        recursiveFileVisitor.visitAllDirectories(path);
    }

    @Test
    void 関連() {
        MethodIdentifiers methods = relationRepository.findConcrete(new MethodIdentifier(
                new Identifier(HogeRepository.class),
                "method",
                new Identifiers(Collections.emptyList())));
        assertThat(methods.list()).isNotEmpty();
    }

    @Test
    void サービス() {
        assertThat(characteristicRepository.find(Characteristic.SERVICE).list()).extracting(Identifier::value)
                .containsExactly(CanonicalService.class.getTypeName());
    }

    @Test
    void 区分() {
        assertThat(characteristicRepository.find(Characteristic.ENUM).list()).hasSize(5);

        assertThat(characteristicRepository.find(Characteristic.ENUM_BEHAVIOUR).list()).extracting(Identifier::value)
                .containsExactly(BehaviourEnum.class.getTypeName(), RichEnum.class.getTypeName());
        assertThat(characteristicRepository.find(Characteristic.ENUM_PARAMETERIZED).list()).extracting(Identifier::value)
                .containsExactly(ParameterizedEnum.class.getTypeName(), RichEnum.class.getTypeName());
        assertThat(characteristicRepository.find(Characteristic.ENUM_POLYMORPHISM).list()).extracting(Identifier::value)
                .containsExactly(PolymorphismEnum.class.getTypeName(), RichEnum.class.getTypeName());
    }

    @Test
    void 識別子() {
        assertThat(characteristicRepository.find(Characteristic.IDENTIFIER).list()).extracting(Identifier::value)
                .containsExactly(SimpleIdentifier.class.getTypeName());
    }

    @Test
    void 数値() {
        assertThat(characteristicRepository.find(Characteristic.NUMBER).list()).extracting(Identifier::value)
                .containsExactly(SimpleNumber.class.getTypeName());
    }

    @Test
    void 日付() {

        assertThat(characteristicRepository.find(Characteristic.DATE).list()).extracting(Identifier::value)
                .containsExactly(SimpleDate.class.getTypeName());
    }

    @Test
    void 期間() {

        assertThat(characteristicRepository.find(Characteristic.TERM).list()).extracting(Identifier::value)
                .containsExactly(SimpleTerm.class.getTypeName());
    }

    @Test
    void コレクション() {
        assertThat(characteristicRepository.find(Characteristic.COLLECTION).list()).extracting(Identifier::value)
                .containsExactly(SimpleCollection.class.getTypeName());
    }

}
