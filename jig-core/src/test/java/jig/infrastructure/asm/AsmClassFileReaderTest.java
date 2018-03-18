package jig.infrastructure.asm;

import jig.domain.model.relation.RelationRepository;
import jig.domain.model.relation.RelationType;
import jig.domain.model.relation.Relations;
import jig.domain.model.tag.Tag;
import jig.domain.model.tag.TagRepository;
import jig.domain.model.thing.Name;
import jig.infrastructure.JigPaths;
import jig.infrastructure.OnMemoryRelationRepository;
import jig.infrastructure.OnMemoryTagRepository;
import jig.infrastructure.RecursiveFileVisitor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class AsmClassFileReaderTest {

    static TagRepository tagRepository = new OnMemoryTagRepository();
    static RelationRepository relationRepository = new OnMemoryRelationRepository();

    @BeforeAll
    static void before() {

        Path path = Paths.get("../sut/build/classes/java/main");

        AsmClassFileReader analyzer = new AsmClassFileReader(tagRepository, relationRepository, new JigPaths());
        RecursiveFileVisitor recursiveFileVisitor = new RecursiveFileVisitor(analyzer::execute);
        recursiveFileVisitor.visitAllDirectories(path);
    }

    @Test
    void 関連() {
        Relations datasources = relationRepository.findTo(new Name("sut.domain.model.hoge.HogeRepository"), RelationType.IMPLEMENT);
        assertThat(datasources.list()).isNotEmpty();

        Relations method = relationRepository.findTo(new Name("sut.domain.model.hoge.HogeRepository.all()"), RelationType.IMPLEMENT);
        assertThat(method.list()).isNotEmpty();
    }

    @Test
    void タグ() {
        assertThat(tagRepository.find(Tag.SERVICE).list()).extracting(Name::value)
                .containsExactlyInAnyOrder(
                        "sut.application.service.CanonicalService",
                        "sut.application.service.ThrowsUnknownExceptionService");

        assertThat(tagRepository.find(Tag.ENUM).list()).hasSize(4);
        assertThat(tagRepository.find(Tag.ENUM_BEHAVIOUR).list()).extracting(Name::value)
                .containsExactly("sut.domain.model.kind.BehaviourEnum");
        assertThat(tagRepository.find(Tag.ENUM_PARAMETERIZED).list()).extracting(Name::value)
                .containsExactly("sut.domain.model.kind.ParameterizedEnum");
        assertThat(tagRepository.find(Tag.ENUM_POLYMORPHISM).list()).extracting(Name::value)
                .containsExactly("sut.domain.model.kind.PolymorphismEnum");

        assertThat(tagRepository.find(Tag.IDENTIFIER).list()).extracting(Name::value)
                .containsExactlyInAnyOrder(
                        "sut.domain.model.fuga.FugaIdentifier",
                        "sut.domain.model.fuga.FugaName");

        assertThat(tagRepository.find(Tag.NUMBER).list()).extracting(Name::value)
                .containsExactly("sut.domain.model.hoge.hogera.HogeraAmount");

        assertThat(tagRepository.find(Tag.DATE).list()).extracting(Name::value)
                .containsExactly("sut.domain.model.hoge.hogera.HogeraDate");

        assertThat(tagRepository.find(Tag.TERM).list()).extracting(Name::value)
                .containsExactly("sut.domain.model.hoge.hogera.HogeraTerm");

        assertThat(tagRepository.find(Tag.COLLECTION).list()).extracting(Name::value)
                .containsExactly("sut.domain.model.hoge.Hoges");
    }

}
