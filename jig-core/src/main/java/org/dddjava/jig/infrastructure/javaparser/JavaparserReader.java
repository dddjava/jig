package org.dddjava.jig.infrastructure.javaparser;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import org.dddjava.jig.domain.model.models.domains.categories.enums.EnumModel;
import org.dddjava.jig.domain.model.parts.classes.method.MethodComment;
import org.dddjava.jig.domain.model.parts.classes.type.ClassComment;
import org.dddjava.jig.domain.model.parts.packages.PackageComment;
import org.dddjava.jig.domain.model.parts.packages.PackageComments;
import org.dddjava.jig.domain.model.sources.file.text.ReadableTextSource;
import org.dddjava.jig.domain.model.sources.file.text.ReadableTextSources;
import org.dddjava.jig.domain.model.sources.jigreader.ClassAndMethodComments;
import org.dddjava.jig.domain.model.sources.jigreader.JavaTextSourceReader;
import org.dddjava.jig.domain.model.sources.jigreader.TextSourceModel;
import org.dddjava.jig.infrastructure.configuration.JigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Javaparserでテキストソースを読み取る
 */
public class JavaparserReader implements JavaTextSourceReader {

    private static Logger LOGGER = LoggerFactory.getLogger(JavaparserReader.class);

    PackageInfoReader packageInfoReader = new PackageInfoReader();
    ClassReader classReader = new ClassReader();

    @Deprecated
    public JavaparserReader() {
    }

    public JavaparserReader(JigProperties properties) {
        Optional.ofNullable(System.getProperty("java.version"))
                .filter(version -> version.startsWith("17"))
                .ifPresent(v -> {
                    ParserConfiguration configuration = StaticJavaParser.getConfiguration();
                    configuration.setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17);
                });

        // TODO プロパティで指定してる場合だけ上書きするようにする
        // configuration.setCharacterEncoding(properties.inputEncoding());
    }

    @Override
    public PackageComments readPackages(ReadableTextSources readableTextSources) {
        List<PackageComment> names = new ArrayList<>();
        for (ReadableTextSource readableTextSource : readableTextSources.list()) {
            packageInfoReader.read(readableTextSource)
                    .ifPresent(names::add);
        }
        return new PackageComments(names);
    }

    @Override
    public TextSourceModel readClasses(ReadableTextSources readableTextSources) {
        List<ClassComment> names = new ArrayList<>();
        List<MethodComment> methodNames = new ArrayList<>();
        List<EnumModel> enums = new ArrayList<>();

        for (ReadableTextSource readableTextSource : readableTextSources.list()) {
            try {
                TypeSourceResult typeSourceResult = classReader.read(readableTextSource);
                typeSourceResult.collectClassComment(names);
                typeSourceResult.collectMethodComments(methodNames);
                typeSourceResult.collectEnum(enums);
            } catch (Exception e) {
                LOGGER.warn("{} のJavadoc読み取りに失敗しました（処理は続行します）", readableTextSource);
                LOGGER.debug("{}読み取り失敗の詳細", readableTextSource, e);
            }
        }
        return new TextSourceModel(new ClassAndMethodComments(names, methodNames), enums);
    }
}