package jig.infrastructure.javaparser;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import jig.domain.model.japanasename.JapaneseName;
import jig.domain.model.japanasename.JapaneseNameRepository;
import jig.domain.model.thing.Name;
import jig.infrastructure.JigPaths;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

@Component
public class PackageInfoReader {

    private final JapaneseNameRepository repository;
    private final JigPaths jigPaths;

    public PackageInfoReader(JapaneseNameRepository repository, JigPaths jigPaths) {
        this.repository = repository;
        this.jigPaths = jigPaths;
    }

    public void execute(Path path) {
        if (!jigPaths.isPackageInfoFile(path)) {
            return;
        }

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
    }
}
