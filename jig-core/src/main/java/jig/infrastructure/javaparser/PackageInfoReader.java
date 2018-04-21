package jig.infrastructure.javaparser;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.nodeTypes.NodeWithName;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import jig.domain.model.identifier.namespace.PackageIdentifier;
import jig.domain.model.japanese.JapaneseName;
import jig.domain.model.japanese.PackageJapaneseName;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Optional;

class PackageInfoReader {

    Optional<PackageJapaneseName> execute(Path path) {
        try {
            CompilationUnit cu = JavaParser.parse(path);
            return cu.getPackageDeclaration()
                    .map(NodeWithName::getNameAsString)
                    .map(PackageIdentifier::new)
                    .map(packageIdentifier -> {
                        JapaneseName japaneseName = cu.accept(new GenericVisitorAdapter<JapaneseName, Void>() {
                            @Override
                            public JapaneseName visit(JavadocComment n, Void arg) {
                                String text = n.parse().getDescription().toText();
                                return new JapaneseName(text);
                            }
                        }, null);

                        return new PackageJapaneseName(packageIdentifier, japaneseName);
                    });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
