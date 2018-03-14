package jig.infrastructure.asm;

import jig.domain.model.tag.Tag;
import jig.domain.model.tag.TagRepository;
import jig.domain.model.relation.RelationRepository;
import jig.domain.model.thing.Name;
import jig.domain.model.thing.ThingRepository;
import jig.infrastructure.OnMemoryRelationRepository;
import jig.infrastructure.OnMemoryTagRepository;
import jig.infrastructure.OnMemoryThingRepository;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class JigClassVisitorTest {


    @Test
    void test() throws IOException {
        TagRepository tagRepository = new OnMemoryTagRepository();
        ThingRepository thingRepository = new OnMemoryThingRepository();
        RelationRepository relationRepository = new OnMemoryRelationRepository();

        Path path = Paths.get("../sut/build/classes/java/main");

        AsmExecutor asmExecutor = new AsmExecutor(tagRepository, thingRepository, relationRepository);
        asmExecutor.load(path);

        assertThat(tagRepository.find(Tag.SERVICE).list()).extracting(Name::value)
                .containsExactlyInAnyOrder(
                        "sut.application.service.CanonicalService",
                        "sut.application.service.ThrowsUnknownExceptionService");

        assertThat(tagRepository.find(Tag.ENUM).list()).extracting(Name::value)
                .containsExactly("sut.domain.model.kind.SimpleEnum");
        assertThat(tagRepository.find(Tag.ENUM_BEHAVIOUR).list()).extracting(Name::value)
                .containsExactly("sut.domain.model.kind.BehaviourEnum");
        assertThat(tagRepository.find(Tag.ENUM_PARAMETERIZED).list()).extracting(Name::value)
                .containsExactly("sut.domain.model.kind.ParameterizedEnum");
        assertThat(tagRepository.find(Tag.ENUM_POLYMORPHISM).list()).extracting(Name::value)
                .containsExactly("sut.domain.model.kind.PolymorphismEnum");
    }

}
