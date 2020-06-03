package org.dddjava.jig.infrastructure.javaparser;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.nodeTypes.NodeWithName;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.description.JavadocDescription;
import org.dddjava.jig.domain.model.declaration.package_.PackageIdentifier;
import org.dddjava.jig.domain.model.jigloaded.alias.Alias;
import org.dddjava.jig.domain.model.jigloaded.alias.JavadocAliasSource;
import org.dddjava.jig.domain.model.jigloaded.alias.PackageAlias;
import org.dddjava.jig.domain.model.jigsource.file.text.javacode.PackageInfoSource;

import java.util.Optional;

class PackageInfoReader {

    Optional<PackageAlias> read(PackageInfoSource packageInfoSource) {
        CompilationUnit cu = StaticJavaParser.parse(packageInfoSource.toInputStream());

        Optional<PackageIdentifier> optPackageIdentifier = cu.getPackageDeclaration()
                .map(NodeWithName::getNameAsString)
                .map(PackageIdentifier::new);

        Optional<Alias> optAlias = getJavadoc(cu)
                .map(Javadoc::getDescription)
                .map(JavadocDescription::toText)
                .map(JavadocAliasSource::new)
                .map(JavadocAliasSource::toAlias);

        return optPackageIdentifier.flatMap(packageIdentifier -> optAlias.map(alias ->
                new PackageAlias(packageIdentifier, alias)));
    }

    private Optional<Javadoc> getJavadoc(CompilationUnit cu) {
        // NodeWithJavadoc#getJavadocでやってることと同じことをする
        return cu.getComment()
                .filter(comment -> comment instanceof JavadocComment)
                .map(comment -> (JavadocComment) comment)
                .map(JavadocComment::parse);
    }
}
