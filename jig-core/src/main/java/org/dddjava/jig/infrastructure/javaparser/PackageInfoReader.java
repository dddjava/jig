package org.dddjava.jig.infrastructure.javaparser;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.nodeTypes.NodeWithName;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.description.JavadocDescription;
import org.dddjava.jig.domain.model.implementation.analyzed.alias.JapaneseName;
import org.dddjava.jig.domain.model.implementation.analyzed.alias.PackageJapaneseName;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.namespace.PackageIdentifier;
import org.dddjava.jig.domain.model.implementation.raw.PackageInfoSource;

import java.util.Optional;

class PackageInfoReader {

    Optional<PackageJapaneseName> read(PackageInfoSource packageInfoSource) {
        CompilationUnit cu = StaticJavaParser.parse(packageInfoSource.toInputStream());

        Optional<PackageIdentifier> optPackageIdentifier = cu.getPackageDeclaration()
                .map(NodeWithName::getNameAsString)
                .map(PackageIdentifier::new);

        Optional<JapaneseName> optJapaneseName = getJavadoc(cu)
                .map(Javadoc::getDescription)
                .map(JavadocDescription::toText)
                .map(JapaneseName::new);

        return optPackageIdentifier.flatMap(packageIdentifier -> optJapaneseName.map(japaneseName ->
                new PackageJapaneseName(packageIdentifier, japaneseName)));
    }

    private Optional<Javadoc> getJavadoc(CompilationUnit cu) {
        // NodeWithJavadoc#getJavadocでやってることと同じことをする
        return cu.getComment()
                .filter(comment -> comment instanceof JavadocComment)
                .map(comment -> (JavadocComment) comment)
                .map(JavadocComment::parse);
    }
}
