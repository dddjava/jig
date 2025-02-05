package org.dddjava.jig.infrastructure.javaparser;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.nodeTypes.NodeWithName;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.description.JavadocDescription;
import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;
import org.dddjava.jig.domain.model.data.term.Term;
import org.dddjava.jig.domain.model.sources.javasources.JavaSourceModel;
import org.dddjava.jig.domain.model.sources.javasources.JavaSourceReader;
import org.dddjava.jig.domain.model.sources.javasources.comment.Comment;
import org.dddjava.jig.domain.model.sources.javasources.comment.PackageComment;
import org.dddjava.jig.infrastructure.configuration.JigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Javaparserで読み取る
 */
public class JavaparserReader implements JavaSourceReader {

    private static final Logger logger = LoggerFactory.getLogger(JavaparserReader.class);

    public JavaparserReader(JigProperties properties) {
        ParserConfiguration configuration = StaticJavaParser.getParserConfiguration();
        configuration.setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17);
        if (Runtime.version().feature() >= 21) {
            configuration.setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21);
        }
        logger.info("javaparser language level: {}", configuration.getLanguageLevel());

        // TODO プロパティで指定してる場合だけ上書きするようにする
        // configuration.setCharacterEncoding(properties.inputEncoding());
    }

    @Override
    public JavaSourceModel parseJavaFile(Path path, Consumer<Term> termCollector) {
        try {
            // StaticJavaParserを変えるときはテストも変えること
            CompilationUnit cu = StaticJavaParser.parse(path);

            if (path.endsWith("package-info.java")) {
                // do-nothing
                return JavaSourceModel.empty();
            } else {
                String packageName = cu.getPackageDeclaration()
                        .map(PackageDeclaration::getNameAsString)
                        .map(name -> name + ".")
                        .orElse("");
                JavaparserClassVisitor classVisitor = new JavaparserClassVisitor(packageName);
                cu.accept(classVisitor, termCollector);
                return classVisitor.javaSourceModel();
            }
        } catch (Exception e) { // IOException以外にJavaparserの例外もキャッチする
            logger.warn("{} の読み取りに失敗しました。このファイルに必要な情報がある場合は欠落します。処理は続行します。", path, e);
            return JavaSourceModel.empty();
        }
    }

    @Override
    public Optional<Term> parsePackageInfoJavaFile(Path path) {
        try {
            // StaticJavaParserを変えるときはテストも変えること
            CompilationUnit cu = StaticJavaParser.parse(path);

            return cu.getPackageDeclaration()
                    .map(NodeWithName::getNameAsString)
                    .map(PackageIdentifier::valueOf)
                    // packageIdentifierがPackageCommentで必要になるのでここはネストにしておく
                    .flatMap(packageIdentifier -> getJavadoc(cu)
                            .map(Javadoc::getDescription)
                            .map(JavadocDescription::toText)
                            .filter(text -> !text.isBlank())
                            .map(javadocDescriptionText -> {
                                return Term.fromPackage(packageIdentifier, javadocDescriptionText);
                            }));
        } catch (Exception e) { // IOException以外にJavaparserの例外もキャッチする
            logger.warn("{} の読み取りに失敗しました。このファイルに必要な情報がある場合は欠落します。処理は続行します。", path, e);
            return Optional.empty();
        }
    }

    Optional<PackageComment> parsePackageInfoJavaFile(CompilationUnit cu) {
        return cu.getPackageDeclaration()
                .map(NodeWithName::getNameAsString)
                .map(PackageIdentifier::valueOf)
                // packageIdentifierがPackageCommentで必要になるのでここはネストにしておく
                .flatMap(packageIdentifier -> getJavadoc(cu)
                        .map(Javadoc::getDescription)
                        .map(JavadocDescription::toText)
                        .filter(text -> !text.isBlank())
                        .map(Comment::fromCodeComment)
                        .map(alias -> new PackageComment(packageIdentifier, alias)));
    }

    private Optional<Javadoc> getJavadoc(CompilationUnit cu) {
        // NodeWithJavadoc#getJavadocでやってることと同じことをする
        return cu.getComment()
                .filter(comment -> comment instanceof JavadocComment)
                .map(comment -> (JavadocComment) comment)
                .map(JavadocComment::parse);
    }
}