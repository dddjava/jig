package org.dddjava.jig.infrastructure.javaparser;

import org.dddjava.jig.domain.model.parts.class_.method.MethodComment;
import org.dddjava.jig.domain.model.parts.class_.type.ClassComment;
import org.dddjava.jig.domain.model.parts.package_.PackageComment;
import org.dddjava.jig.domain.model.parts.package_.PackageComments;
import org.dddjava.jig.domain.model.sources.file.text.javacode.JavaSource;
import org.dddjava.jig.domain.model.sources.file.text.javacode.JavaSources;
import org.dddjava.jig.domain.model.sources.file.text.javacode.PackageInfoSource;
import org.dddjava.jig.domain.model.sources.file.text.javacode.PackageInfoSources;
import org.dddjava.jig.domain.model.sources.jigreader.ClassAndMethodComments;
import org.dddjava.jig.domain.model.sources.jigreader.JavaSourceAliasReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * JavaparserでJavadocから別名を取得する実装
 */
public class JavaparserAliasReader implements JavaSourceAliasReader {

    private static Logger LOGGER = LoggerFactory.getLogger(JavaparserAliasReader.class);

    PackageInfoReader packageInfoReader = new PackageInfoReader();
    ClassReader classReader = new ClassReader();

    @Override
    public PackageComments readPackages(PackageInfoSources nameSources) {
        List<PackageComment> names = new ArrayList<>();
        for (PackageInfoSource packageInfoSource : nameSources.list()) {
            packageInfoReader.read(packageInfoSource)
                    .ifPresent(names::add);
        }
        return new PackageComments(names);
    }

    @Override
    public ClassAndMethodComments readAlias(JavaSources javaSources) {
        List<ClassComment> names = new ArrayList<>();
        List<MethodComment> methodNames = new ArrayList<>();

        for (JavaSource javaSource : javaSources.list()) {
            try {
                TypeSourceResult typeSourceResult = classReader.read(javaSource);
                ClassComment classComment = typeSourceResult.classComment;
                if (classComment != null) {
                    names.add(classComment);
                }
                methodNames.addAll(typeSourceResult.methodComments);
            } catch (Exception e) {
                LOGGER.warn("{} のJavadoc読み取りに失敗しました（処理は続行します）", javaSource);
                LOGGER.debug("{}読み取り失敗の詳細", javaSource, e);
            }
        }
        return new ClassAndMethodComments(names, methodNames);
    }
}