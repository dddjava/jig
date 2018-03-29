package jig.infrastructure.javaparser;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import jig.domain.model.identifier.TypeIdentifier;
import jig.domain.model.japanasename.JapaneseName;
import jig.domain.model.japanasename.JapaneseNameRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

public class ClassCommentReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClassCommentReader.class);

    private final JapaneseNameRepository repository;

    public ClassCommentReader(JapaneseNameRepository repository) {
        this.repository = repository;
    }

    void execute(Path path) {
        LOGGER.debug("コメント取り込み: {}", path);
        try {
            CompilationUnit cu = JavaParser.parse(path);

            cu.accept(new VoidVisitorAdapter<Void>() {

                private PackageDeclaration packageDeclaration = null;

                @Override
                public void visit(PackageDeclaration packageDeclaration, Void arg) {
                    this.packageDeclaration = packageDeclaration;
                }

                @Override
                public void visit(ClassOrInterfaceDeclaration classOrInterfaceDeclaration, Void arg) {
                    String className = classOrInterfaceDeclaration.getNameAsString();
                    if (packageDeclaration != null) {
                        className = packageDeclaration.getNameAsString() + "." + className;
                    }

                    TypeIdentifier typeIdentifier = new TypeIdentifier(className);

                    classOrInterfaceDeclaration.accept(new VoidVisitorAdapter<TypeIdentifier>() {

                        @Override
                        public void visit(JavadocComment n, TypeIdentifier typeIdentifier) {
                            n.getCommentedNode()
                                    .filter(node -> node instanceof ClassOrInterfaceDeclaration)
                                    .ifPresent(node -> {
                                        String text = n.parse().getDescription().toText();
                                        JapaneseName japaneseName = new JapaneseName(text);

                                        repository.register(typeIdentifier, japaneseName);
                                    });
                        }
                    }, typeIdentifier);
                }
            }, null);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
