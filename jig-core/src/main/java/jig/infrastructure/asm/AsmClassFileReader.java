package jig.infrastructure.asm;

import jig.domain.model.characteristic.CharacteristicRepository;
import jig.domain.model.relation.RelationRepository;
import jig.infrastructure.JigPaths;
import org.objectweb.asm.ClassReader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class AsmClassFileReader {

    private final CharacteristicRepository characteristicRepository;
    private final RelationRepository relationRepository;
    private final JigPaths jigPaths;

    public AsmClassFileReader(CharacteristicRepository characteristicRepository, RelationRepository relationRepository, JigPaths jigPaths) {
        this.characteristicRepository = characteristicRepository;
        this.relationRepository = relationRepository;
        this.jigPaths = jigPaths;
    }

    public void execute(Path file) {
        if (!jigPaths.isClassFile(file)) {
            return;
        }

        try (InputStream inputStream = Files.newInputStream(file)) {
            ClassReader classReader = new ClassReader(inputStream);
            classReader.accept(new RelationReadingVisitor(relationRepository), ClassReader.SKIP_DEBUG);
            classReader.accept(new CharacteristicClassVisitor(characteristicRepository), ClassReader.SKIP_DEBUG);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}