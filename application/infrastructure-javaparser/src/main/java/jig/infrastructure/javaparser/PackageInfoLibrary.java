package jig.infrastructure.javaparser;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import jig.model.tag.JapaneseName;
import jig.model.tag.JapaneseNameDictionary;
import jig.model.tag.JapaneseNameDictionaryLibrary;
import jig.model.thing.Name;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class PackageInfoLibrary implements JapaneseNameDictionaryLibrary {

    public PackageInfoLibrary(Path rootPath) {
        this.rootPath = rootPath;
    }

    private Path rootPath;

    @Override
    public JapaneseNameDictionary borrow() {
        JapaneseNameDictionary dictionary = new JapaneseNameDictionary();
        if (Files.notExists(rootPath)) {
            return dictionary;
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
                                dictionary.register(name, japaneseName);
                            }
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });

            return dictionary;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
