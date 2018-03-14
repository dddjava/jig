package jig.infrastructure.javaparser;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import jig.domain.model.japanasename.JapaneseName;
import jig.domain.model.japanasename.JapaneseNameRepository;
import jig.domain.model.thing.Name;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class PackageInfoReader {

    private Path rootPath;

    public PackageInfoReader(Path rootPath) {
        this.rootPath = rootPath;
    }

    public void registerTo(JapaneseNameRepository repository) {
        if (Files.notExists(rootPath)) {
            return;
        }

        try (Stream<Path> walk = Files.walk(rootPath)) {
            walk.filter(path -> path.toFile().getName().equals("package-info.java"))
                    .forEach(path -> {
                        try {
                            CompilationUnit cu = JavaParser.parse(path);
                            Name name = cu.accept(new GenericVisitorAdapter<Name, Void>() {
                                @Override
                                public Name visit(PackageDeclaration n, Void arg) {
                                    String name = n.getNameAsString();
                                    return new Name(name);
                                }
                            }, null);

                            JapaneseName japaneseName = cu.accept(new GenericVisitorAdapter<JapaneseName, Void>() {
                                @Override
                                public JapaneseName visit(JavadocComment n, Void arg) {
                                    String text = n.parse().getDescription().toText();
                                    return new JapaneseName(text);
                                }
                            }, null);

                            if (name != null && japaneseName != null) {
                                repository.register(name, japaneseName);
                            }
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
