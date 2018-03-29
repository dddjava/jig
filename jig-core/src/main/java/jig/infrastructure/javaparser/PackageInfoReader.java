package jig.infrastructure.javaparser;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import jig.domain.model.identifier.namespace.PackageIdentifier;
import jig.domain.model.japanasename.JapaneseName;
import jig.domain.model.japanasename.JapaneseNameRepository;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

public class PackageInfoReader {

    private final JapaneseNameRepository repository;

    public PackageInfoReader(JapaneseNameRepository repository) {
        this.repository = repository;
    }

    void execute(Path path) {
        try {
            CompilationUnit cu = JavaParser.parse(path);
            PackageIdentifier packageIdentifier = cu.accept(new GenericVisitorAdapter<PackageIdentifier, Void>() {
                @Override
                public PackageIdentifier visit(PackageDeclaration n, Void arg) {
                    String name = n.getNameAsString();
                    return new PackageIdentifier(name);
                }
            }, null);

            JapaneseName japaneseName = cu.accept(new GenericVisitorAdapter<JapaneseName, Void>() {
                @Override
                public JapaneseName visit(JavadocComment n, Void arg) {
                    String text = n.parse().getDescription().toText();
                    return new JapaneseName(text);
                }
            }, null);

            if (packageIdentifier != null && japaneseName != null) {
                repository.register(packageIdentifier, japaneseName);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
