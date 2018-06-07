package org.dddjava.jig.infrastructure.javaparser;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.nodeTypes.NodeWithName;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.description.JavadocDescription;
import org.dddjava.jig.domain.model.declaration.namespace.PackageIdentifier;
import org.dddjava.jig.domain.model.japanese.JapaneseName;
import org.dddjava.jig.domain.model.japanese.PackageJapaneseName;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Optional;

class PackageInfoReader {

    Optional<PackageJapaneseName> read(Path path) {
        try {
            CompilationUnit cu = JavaParser.parse(path);

            Optional<PackageIdentifier> optPackageIdentifier = cu.getPackageDeclaration()
                    .map(NodeWithName::getNameAsString)
                    .map(PackageIdentifier::new);

            Optional<JapaneseName> optJapaneseName = getJavadoc(cu)
                    .map(Javadoc::getDescription)
                    .map(JavadocDescription::toText)
                    .map(JapaneseName::new);

            return optPackageIdentifier.flatMap(packageIdentifier -> optJapaneseName.map(japaneseName ->
                    new PackageJapaneseName(packageIdentifier, japaneseName)));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Optional<Javadoc> getJavadoc(CompilationUnit cu) {
        // NodeWithJavadoc#getJavadocでやってることと同じことをする
        return cu.getComment()
                .filter(comment -> comment instanceof JavadocComment)
                .map(comment -> (JavadocComment) comment)
                .map(JavadocComment::parse);
    }
}
