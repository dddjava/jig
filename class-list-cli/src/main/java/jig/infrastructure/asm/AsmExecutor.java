package jig.infrastructure.asm;

import jig.domain.model.tag.TagRepository;
import jig.domain.model.relation.RelationRepository;
import jig.domain.model.thing.ThingRepository;
import org.objectweb.asm.ClassReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class AsmExecutor {

    private final TagRepository tagRepository;
    private final ThingRepository thingRepository;
    private final RelationRepository relationRepository;

    public AsmExecutor(TagRepository tagRepository, ThingRepository thingRepository, RelationRepository relationRepository) {
        this.tagRepository = tagRepository;
        this.thingRepository = thingRepository;
        this.relationRepository = relationRepository;
    }

    public void load(Path... paths) {
        for (Path path : paths) {
            loadInternal(path);
        }
    }

    private void loadInternal(Path path) {
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

                    try (InputStream inputStream = Files.newInputStream(file)) {
                        ClassReader classReader = new ClassReader(inputStream);

                        classReader.accept(new JigClassVisitor(tagRepository, thingRepository, relationRepository), ClassReader.SKIP_DEBUG);
                        return super.visitFile(file, attrs);
                    }
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
