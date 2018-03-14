package jig.infrastructure.asm;

import jig.domain.model.list.kind.Tag;
import jig.domain.model.list.kind.TagRepository;
import jig.domain.model.thing.Name;
import jig.infrastructure.OnMemoryTagRepository;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import static org.assertj.core.api.Assertions.assertThat;

public class JigClassVisitorTest {

    @Test
    void test() throws IOException {
        TagRepository tagRepository = new OnMemoryTagRepository();

        Path path = Paths.get("../sut/build/classes/java/main");

        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

                try (InputStream inputStream = Files.newInputStream(file)) {
                    ClassReader classReader = new ClassReader(inputStream);

                    classReader.accept(new JigClassVisitor(tagRepository), ClassReader.SKIP_DEBUG);
                }

                return super.visitFile(file, attrs);
            }
        });

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
