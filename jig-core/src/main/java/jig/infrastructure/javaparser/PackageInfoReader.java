package jig.infrastructure.javaparser;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import jig.domain.model.identifier.Identifier;
import jig.domain.model.japanasename.JapaneseName;
import jig.domain.model.japanasename.JapaneseNameRepository;
import jig.infrastructure.JigPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class PackageInfoReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(PackageInfoReader.class);

    private final JapaneseNameRepository repository;
    private final JigPaths jigPaths;

    public PackageInfoReader(JapaneseNameRepository repository, JigPaths jigPaths) {
        this.repository = repository;
        this.jigPaths = jigPaths;
    }

    public void execute(Path rootPath) {
        try {
            for (Path path : jigPaths.extractSourcePath(rootPath)) {
                Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        if (jigPaths.isPackageInfoFile(file)) executeInternal(file);
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void executeInternal(Path path) {
        LOGGER.debug("parsing: {}", path);
        try {
            CompilationUnit cu = JavaParser.parse(path);
            Identifier identifier = cu.accept(new GenericVisitorAdapter<Identifier, Void>() {
                @Override
                public Identifier visit(PackageDeclaration n, Void arg) {
                    String name = n.getNameAsString();
                    return new Identifier(name);
                }
            }, null);

            JapaneseName japaneseName = cu.accept(new GenericVisitorAdapter<JapaneseName, Void>() {
                @Override
                public JapaneseName visit(JavadocComment n, Void arg) {
                    String text = n.parse().getDescription().toText();
                    return new JapaneseName(text);
                }
            }, null);

            if (identifier != null && japaneseName != null) {
                repository.register(identifier, japaneseName);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
