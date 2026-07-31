package org.dddjava.jig.infrastructure.javaparser;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.nodeTypes.NodeWithName;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.description.JavadocDescription;
import org.dddjava.jig.application.GlossaryRepository;
import org.dddjava.jig.domain.model.data.packages.PackageId;
import org.dddjava.jig.domain.model.data.terms.TermId;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.sources.javasources.JavaSourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Javaparserで読み取る
 */
public class JavaparserReader {

    private static final Logger logger = LoggerFactory.getLogger(JavaparserReader.class);

    private final JavaParser javaParser;

    public JavaparserReader() {
        ParserConfiguration configuration = new ParserConfiguration();
        configuration.setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17);
        if (Runtime.version().feature() >= 25) {
            configuration.setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_25);
        } else if (Runtime.version().feature() >= 21) {
            configuration.setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21);
        }
        logger.info("javaparser language level: {}", configuration.getLanguageLevel());

        // TODO プロパティで指定してる場合だけ上書きするようにする
        // configuration.setCharacterEncoding(properties.inputEncoding());
        this.javaParser = new JavaParser(configuration);
    }

    public ParseResult parseJavaFile(Path path, GlossaryRepository glossaryRepository) {
        try {
            var parseResult = javaParser.parse(path);
            if (!parseResult.isSuccessful()) {
                throw new IllegalStateException(path + " のパースに失敗しました");
            }
            CompilationUnit cu = parseResult.getResult()
                    .orElseThrow(() -> new IllegalStateException(path + " のパースに失敗しました"));

            String packageName = cu.getPackageDeclaration()
                    .map(PackageDeclaration::getNameAsString)
                    .map(name -> name + ".")
                    .orElse("");
            JavaparserClassVisitor classVisitor = new JavaparserClassVisitor(packageName);
            cu.accept(classVisitor, glossaryRepository);
            return new ParseResult(classVisitor.javaSourceModel(), classVisitor.declaredTypeIds(), path, true);
        } catch (Exception e) { // IOException以外にJavaparserの例外もキャッチする
            logger.warn("{} の読み取りに失敗しました。このファイルに必要な情報がある場合は欠落します。このエラーはローカルenumが存在する場合などに発生します。処理は続行します。", path, e);
            return ParseResult.empty(path);
        }
    }

    /**
     * 1つのJavaファイルのパース結果
     */
    public record ParseResult(JavaSourceModel sourceModel, List<TypeId> declaredTypeIds, Path sourcePath, boolean succeeded) {
        public ParseResult(JavaSourceModel sourceModel, List<TypeId> declaredTypeIds, Path sourcePath) {
            this(sourceModel, declaredTypeIds, sourcePath, true);
        }

        public static ParseResult empty(Path sourcePath) {
            return new ParseResult(JavaSourceModel.empty(), List.of(), sourcePath, false);
        }
    }

    public Optional<PackageId> loadPackageInfoJavaFile(Path path, GlossaryRepository glossaryRepository) {
        return parsePackageInfoJavaFile(path, glossaryRepository).packageId();
    }

    public PackageInfoParseResult parsePackageInfoJavaFile(Path path, GlossaryRepository glossaryRepository) {
        try {
            var parseResult = javaParser.parse(path);
            if (!parseResult.isSuccessful()) {
                throw new IllegalStateException(path + " のパースに失敗しました");
            }
            CompilationUnit cu = parseResult.getResult()
                    .orElseThrow(() -> new IllegalStateException(path + " のパースに失敗しました"));

            return new PackageInfoParseResult(loadPackageInfoJavaFile(cu, glossaryRepository), path, true);
        } catch (Exception e) { // IOException以外にJavaparserの例外もキャッチする
            logger.warn("{} の読み取りに失敗しました。このファイルに必要な情報がある場合は欠落します。処理は続行します。", path, e);
            return new PackageInfoParseResult(Optional.empty(), path, false);
        }
    }

    public record PackageInfoParseResult(Optional<PackageId> packageId, Path sourcePath, boolean succeeded) {
    }

    Optional<PackageId> loadPackageInfoJavaFile(CompilationUnit cu, GlossaryRepository glossaryRepository) {
        // packageIdがPackageCommentで必要になるのでここはネストにしておく
        Optional<PackageId> packageIdOpt = cu.getPackageDeclaration()
                .map(NodeWithName::getNameAsString)
                .map(PackageId::valueOf);
        packageIdOpt.flatMap(packageId -> {
            TermId termId = glossaryRepository.fromPackageId(packageId);
            return getJavadoc(cu)
                    .map(Javadoc::getDescription)
                    .map(JavadocDescription::toText)
                    .filter(text -> !text.isBlank())
                    .map(javadocText -> TermFactory.fromPackage(termId, javadocText));
        }).ifPresent(glossaryRepository::register);
        return packageIdOpt;
    }

    private Optional<Javadoc> getJavadoc(CompilationUnit cu) {
        // NodeWithJavadoc#getJavadocでやってることと同じことをする
        return cu.getComment()
                .filter(comment -> comment instanceof JavadocComment)
                .map(comment -> (JavadocComment) comment)
                .map(JavadocComment::parse);
    }
}
