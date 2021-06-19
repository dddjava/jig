package org.dddjava.jig.infrastructure.javaparser;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.nodeTypes.NodeWithName;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.description.JavadocDescription;
import org.dddjava.jig.domain.model.parts.comment.Comment;
import org.dddjava.jig.domain.model.parts.packages.PackageComment;
import org.dddjava.jig.domain.model.parts.packages.PackageIdentifier;
import org.dddjava.jig.domain.model.sources.file.text.ReadableTextSource;

import java.util.Optional;

class PackageInfoReader {

    Optional<PackageComment> read(ReadableTextSource codeSource) {
        CompilationUnit cu = StaticJavaParser.parse(codeSource.toInputStream());

        Optional<PackageIdentifier> optPackageIdentifier = cu.getPackageDeclaration()
                .map(NodeWithName::getNameAsString)
                .map(PackageIdentifier::new);

        Optional<Comment> optAlias = getJavadoc(cu)
                .map(Javadoc::getDescription)
                .map(JavadocDescription::toText)
                .map(Comment::fromCodeComment);

        return optPackageIdentifier.flatMap(packageIdentifier -> optAlias.map(alias ->
                new PackageComment(packageIdentifier, alias)));
    }

    private Optional<Javadoc> getJavadoc(CompilationUnit cu) {
        // NodeWithJavadoc#getJavadocでやってることと同じことをする
        return cu.getComment()
                .filter(comment -> comment instanceof JavadocComment)
                .map(comment -> (JavadocComment) comment)
                .map(JavadocComment::parse);
    }
}
