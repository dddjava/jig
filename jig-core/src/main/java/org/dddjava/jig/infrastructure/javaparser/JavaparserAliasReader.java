package org.dddjava.jig.infrastructure.javaparser;

import org.dddjava.jig.domain.model.implementation.analyzed.alias.*;
import org.dddjava.jig.domain.model.implementation.raw.JavaSource;
import org.dddjava.jig.domain.model.implementation.raw.JavaSources;
import org.dddjava.jig.domain.model.implementation.raw.packageinfo.PackageInfoSource;
import org.dddjava.jig.domain.model.implementation.raw.packageinfo.PackageInfoSources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * JavaparserでJavadocから別名を取得する実装
 */
public class JavaparserAliasReader implements AliasReader {

    private static Logger LOGGER = LoggerFactory.getLogger(JavaparserAliasReader.class);

    PackageInfoReader packageInfoReader = new PackageInfoReader();
    ClassReader classReader = new ClassReader();

    @Override
    public PackageNames readPackages(PackageInfoSources nameSources) {
        List<PackageAlias> names = new ArrayList<>();
        for (PackageInfoSource packageInfoSource : nameSources.list()) {
            packageInfoReader.read(packageInfoSource)
                    .ifPresent(names::add);
        }
        return new PackageNames(names);
    }

    @Override
    public TypeNames readTypes(JavaSources nameSources) {
        List<TypeAlias> names = new ArrayList<>();
        List<MethodAlias> methodNames = new ArrayList<>();

        for (JavaSource javaSource : nameSources.list()) {
            try {
                TypeSourceResult typeSourceResult = classReader.read(javaSource);
                TypeAlias typeAlias = typeSourceResult.typeAlias;
                if (typeAlias != null) {
                    names.add(typeAlias);
                }
                methodNames.addAll(typeSourceResult.methodAliases);
            } catch (Exception e) {
                LOGGER.warn("{} のJavadoc読み取りに失敗しました（処理は続行します）", javaSource);
                LOGGER.debug("{}読み取り失敗の詳細", javaSource, e);
            }
        }
        return new TypeNames(names, methodNames);
    }
}