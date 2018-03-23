package jig.infrastructure.javaparser;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import jig.domain.model.identifier.Identifier;
import jig.domain.model.japanasename.JapaneseName;
import jig.domain.model.japanasename.JapaneseNameRepository;
import jig.infrastructure.JigPaths;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

@Component
public class ClassCommentReader {

    private final JapaneseNameRepository repository;
    private final JigPaths jigPaths;

    public ClassCommentReader(JapaneseNameRepository repository, JigPaths jigPaths) {
        this.repository = repository;
        this.jigPaths = jigPaths;
    }

    public void execute(Path path) {
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    executeInternal(file);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void executeInternal(Path path) {
        if (!jigPaths.isJavaFile(path)) {
            return;
        }

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

                    Identifier identifier = new Identifier(className);

                    classOrInterfaceDeclaration.accept(new VoidVisitorAdapter<Identifier>() {

                        @Override
                        public void visit(JavadocComment n, Identifier identifier) {
                            n.getCommentedNode()
                                    .filter(node -> node instanceof ClassOrInterfaceDeclaration)
                                    .ifPresent(node -> {
                                        String text = n.parse().getDescription().toText();
                                        JapaneseName japaneseName = new JapaneseName(text);

                                        repository.register(identifier, japaneseName);
                                    });
                        }
                    }, identifier);
                }
            }, null);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
