package jig.analizer.javaparser;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import jig.analizer.dependency.FullQualifiedName;
import jig.analizer.dependency.JapaneseName;
import jig.analizer.dependency.JapaneseNameRepository;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class PackageInfoParser {

    public PackageInfoParser(Path rootPath) {
        this.rootPath = rootPath;
    }

    private Path rootPath;

    public JapaneseNameRepository parse() {
        try (Stream<Path> walk = Files.walk(rootPath)) {
            JapaneseNameRepository repository = new JapaneseNameRepository();

            walk.filter(path -> path.toFile().getName().equals("package-info.java"))
                    .forEach(path -> {
                        try {
                            CompilationUnit cu = JavaParser.parse(path);
                            FullQualifiedName fqn = cu.accept(new GenericVisitorAdapter<FullQualifiedName, Void>() {
                                @Override
                                public FullQualifiedName visit(PackageDeclaration n, Void arg) {
                                    String name = n.getNameAsString();
                                    return new FullQualifiedName(name);
                                }
                            }, null);

                            JapaneseName japaneseName = cu.accept(new GenericVisitorAdapter<JapaneseName, Void>() {
                                @Override
                                public JapaneseName visit(JavadocComment n, Void arg) {
                                    String text = n.parse().getDescription().toText();
                                    return new JapaneseName(text);
                                }
                            }, null);

                            repository.register(fqn, japaneseName);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });

            return repository;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
