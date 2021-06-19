package org.dddjava.jig.infrastructure.javaparser;

import org.dddjava.jig.domain.model.parts.classes.method.MethodComment;
import org.dddjava.jig.domain.model.parts.classes.type.ClassComment;
import org.dddjava.jig.domain.model.parts.packages.PackageComment;
import org.dddjava.jig.domain.model.parts.packages.PackageComments;
import org.dddjava.jig.domain.model.sources.file.text.ReadableTextSource;
import org.dddjava.jig.domain.model.sources.file.text.ReadableTextSources;
import org.dddjava.jig.domain.model.sources.jigreader.ClassAndMethodComments;
import org.dddjava.jig.domain.model.sources.jigreader.JavaTextSourceReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * JavaparserでJavadocから別名を取得する実装
 */
public class JavaparserReader implements JavaTextSourceReader {

    private static Logger LOGGER = LoggerFactory.getLogger(JavaparserReader.class);

    PackageInfoReader packageInfoReader = new PackageInfoReader();
    ClassReader classReader = new ClassReader();

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
    public ClassAndMethodComments readClasses(ReadableTextSources readableTextSources) {
        List<ClassComment> names = new ArrayList<>();
        List<MethodComment> methodNames = new ArrayList<>();

        for (ReadableTextSource readableTextSource : readableTextSources.list()) {
            try {
                TypeSourceResult typeSourceResult = classReader.read(readableTextSource);
                ClassComment classComment = typeSourceResult.classComment;
                if (classComment != null) {
                    names.add(classComment);
                }
                methodNames.addAll(typeSourceResult.methodComments);
            } catch (Exception e) {
                LOGGER.warn("{} のJavadoc読み取りに失敗しました（処理は続行します）", readableTextSource);
                LOGGER.debug("{}読み取り失敗の詳細", readableTextSource, e);
            }
        }
        return new ClassAndMethodComments(names, methodNames);
    }
}