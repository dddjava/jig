package org.dddjava.jig.infrastructure.javaparser;

import org.dddjava.jig.domain.model.implementation.analyzed.japanese.*;
import org.dddjava.jig.domain.model.implementation.raw.JavaSource;
import org.dddjava.jig.domain.model.implementation.raw.JavaSources;
import org.dddjava.jig.domain.model.implementation.raw.PackageInfoSource;
import org.dddjava.jig.domain.model.implementation.raw.PackageInfoSources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class JavaparserJapaneseReader implements JapaneseReader {

    private static Logger LOGGER = LoggerFactory.getLogger(JavaparserJapaneseReader.class);

    PackageInfoReader packageInfoReader = new PackageInfoReader();
    ClassReader classReader = new ClassReader();

    @Override
    public PackageNames readPackages(PackageInfoSources nameSources) {
        List<PackageJapaneseName> names = new ArrayList<>();
        for (PackageInfoSource packageInfoSource : nameSources.list()) {
            packageInfoReader.read(packageInfoSource)
                    .ifPresent(names::add);
        }
        return new PackageNames(names);
    }

    @Override
    public TypeNames readTypes(JavaSources nameSources) {
        List<TypeJapaneseName> names = new ArrayList<>();
        List<MethodJapaneseName> methodNames = new ArrayList<>();

        for (JavaSource javaSource : nameSources.list()) {
            try {
                TypeSourceResult typeSourceResult = classReader.read(javaSource);
                TypeJapaneseName typeJapaneseName = typeSourceResult.typeJapaneseName;
                if (typeJapaneseName != null) {
                    names.add(typeJapaneseName);
                }
                methodNames.addAll(typeSourceResult.methodJapaneseNames);
            } catch (Exception e) {
                LOGGER.warn("{} のJavadoc読み取りに失敗しました（処理は続行します）", javaSource);
                LOGGER.debug("{}読み取り失敗の詳細", javaSource, e);
            }
        }
        return new TypeNames(names, methodNames);
    }
}