package jig.infrastructure.asm;

import jig.domain.model.relation.RelationRepository;
import jig.domain.model.tag.TagRepository;
import org.objectweb.asm.ClassReader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class AsmClassFileReader {

    private final TagRepository tagRepository;
    private final RelationRepository relationRepository;

    public AsmClassFileReader(TagRepository tagRepository, RelationRepository relationRepository) {
        this.tagRepository = tagRepository;
        this.relationRepository = relationRepository;
    }

    public void execute(Path file) {
        if (!file.toString().endsWith(".class")) {
            return;
        }

        try (InputStream inputStream = Files.newInputStream(file)) {
            ClassReader classReader = new ClassReader(inputStream);
            classReader.accept(new RelationReadingVisitor(relationRepository), ClassReader.SKIP_DEBUG);
            classReader.accept(new TagReadingVisitor(tagRepository), ClassReader.SKIP_DEBUG);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}