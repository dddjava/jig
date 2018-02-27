package jig.infrastructure.javaparser;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import jig.model.tag.JapaneseName;
import jig.model.tag.JapaneseNameDictionary;
import jig.model.thing.Name;

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

    public JapaneseNameDictionary parseClass() {
        JapaneseNameDictionary repository = new JapaneseNameDictionary();
        if (Files.notExists(rootPath)) {
            return repository;
        }

        try (Stream<Path> walk = Files.walk(rootPath)) {

            walk.filter(path -> path.toFile().getName().endsWith(".java"))
                    .forEach(path -> {
                        try {
                            CompilationUnit cu = JavaParser.parse(path);
                            com.github.javaparser.ast.expr.Name packageName = cu.accept(new GenericVisitorAdapter<com.github.javaparser.ast.expr.Name, Void>() {
                                @Override
                                public com.github.javaparser.ast.expr.Name visit(PackageDeclaration n, Void arg) {
                                    return n.getName();
                                }
                            }, null);

                            cu.accept(new VoidVisitorAdapter<com.github.javaparser.ast.expr.Name>() {
                                @Override
                                public void visit(ClassOrInterfaceDeclaration n, com.github.javaparser.ast.expr.Name packageName) {
                                    Name fullQualifiedName = new Name(
                                            packageName.asString() + "." + n.getNameAsString()
                                    );

                                    n.accept(new VoidVisitorAdapter<Name>() {
                                        @Override
                                        public void visit(JavadocComment n, Name name) {
                                            String text = n.parse().getDescription().toText();
                                            JapaneseName japaneseName = new JapaneseName(text);
                                            repository.register(name, japaneseName);
                                        }
                                    }, fullQualifiedName);
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

    public JapaneseNameDictionary parse() {
        JapaneseNameDictionary repository = new JapaneseNameDictionary();
        if (Files.notExists(rootPath)) {
            return repository;
        }

        try (Stream<Path> walk = Files.walk(rootPath)) {

            walk.filter(path -> path.toFile().getName().equals("package-info.java"))
                    .forEach(path -> {
                        try {
                            CompilationUnit cu = JavaParser.parse(path);
                            Name fqn = cu.accept(new GenericVisitorAdapter<Name, Void>() {
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
