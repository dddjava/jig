package jig.analizer.javaparser;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import jig.domain.model.dependency.FullQualifiedName;
import jig.domain.model.dependency.JapaneseName;
import jig.domain.model.dependency.JapaneseNameRepository;

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


    public JapaneseNameRepository parseClass() {
        JapaneseNameRepository repository = new JapaneseNameRepository();
        if (Files.notExists(rootPath)) {
            return repository;
        }

        try (Stream<Path> walk = Files.walk(rootPath)) {

            walk.filter(path -> path.toFile().getName().endsWith(".java"))
                    .forEach(path -> {
                        try {
                            CompilationUnit cu = JavaParser.parse(path);
                            Name packageName = cu.accept(new GenericVisitorAdapter<Name, Void>() {
                                @Override
                                public Name visit(PackageDeclaration n, Void arg) {
                                    return n.getName();
                                }
                            }, null);

                            cu.accept(new VoidVisitorAdapter<Name>() {
                                @Override
                                public void visit(ClassOrInterfaceDeclaration n, Name packageName) {
                                    FullQualifiedName fullQualifiedName = new FullQualifiedName(
                                            packageName.asString() + "." + n.getNameAsString()
                                    );

                                    if (n.isAnnotationPresent("Service")) {
                                        n.accept(new VoidVisitorAdapter<FullQualifiedName>() {
                                            @Override
                                            public void visit(JavadocComment n, FullQualifiedName name) {
                                                String text = n.parse().getDescription().toText();
                                                JapaneseName japaneseName = new JapaneseName(text);
                                                repository.register(name, japaneseName);
                                            }
                                        }, fullQualifiedName);
                                    }
                                }
                            }, packageName);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });

            return repository;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public JapaneseNameRepository parse() {
        JapaneseNameRepository repository = new JapaneseNameRepository();
        if (Files.notExists(rootPath)) {
            return repository;
        }

        try (Stream<Path> walk = Files.walk(rootPath)) {

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

                            if (fqn != null && japaneseName != null) {
                                repository.register(fqn, japaneseName);
                            }
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
