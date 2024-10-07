package org.dddjava.jig.infrastructure.javaparser;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import org.dddjava.jig.domain.model.parts.packages.PackageComment;
import org.dddjava.jig.domain.model.sources.file.text.ReadableTextSource;
import org.dddjava.jig.domain.model.sources.file.text.ReadableTextSources;
import org.dddjava.jig.domain.model.sources.file.text.TextSources;
import org.dddjava.jig.domain.model.sources.jigfactory.TextSourceModel;
import org.dddjava.jig.domain.model.sources.jigreader.JavaTextSourceReader;
import org.dddjava.jig.infrastructure.configuration.JigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Javaparserでテキストソースを読み取る
 */
public class JavaparserReader implements JavaTextSourceReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(JavaparserReader.class);

    PackageInfoReader packageInfoReader = new PackageInfoReader();

    public JavaparserReader(JigProperties properties) {
        if (Runtime.version().feature() >= 17) {
            ParserConfiguration configuration = StaticJavaParser.getParserConfiguration();
            configuration.setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17);
        }

        // TODO プロパティで指定してる場合だけ上書きするようにする
        // configuration.setCharacterEncoding(properties.inputEncoding());
    }

    @Override
    public TextSourceModel textSourceModel(TextSources textSources) {
        ReadableTextSources readableTextSources = textSources.javaSources();
        TextSourceModel javaTextSourceModel = readableTextSources.list().stream()
                .map(readableTextSource -> {
                    try (InputStream inputStream = readableTextSource.toInputStream()) {
                        return read(inputStream);
                    } catch (Exception e) {
                        LOGGER.warn("{} のソースコード読み取りに失敗しました（処理は続行します）", readableTextSource);
                        LOGGER.debug("{}読み取り失敗の詳細", readableTextSource, e);
                        return TextSourceModel.empty();
                    }
                })
                .reduce(TextSourceModel::merge)
                .orElseGet(() -> TextSourceModel.empty());

        List<PackageComment> names = new ArrayList<>();
        for (ReadableTextSource readableTextSource : textSources.packageInfoSources().list()) {
            packageInfoReader.read(readableTextSource)
                    .ifPresent(names::add);
        }
        javaTextSourceModel.addPackageComment(names);

        return javaTextSourceModel;
    }

    TextSourceModel read(InputStream inputStream) {
        CompilationUnit cu = StaticJavaParser.parse(inputStream);

        String packageName = cu.getPackageDeclaration()
                .map(PackageDeclaration::getNameAsString)
                .map(name -> name + ".")
                .orElse("");

        ClassVisitor typeVisitor = new ClassVisitor(packageName);
        AdditionalSourceModelBuilder builder = new AdditionalSourceModelBuilder();
        cu.accept(typeVisitor, builder);
        return typeVisitor.toTextSourceModel();
    }
}