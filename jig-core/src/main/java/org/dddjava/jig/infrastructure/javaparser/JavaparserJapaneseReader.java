package org.dddjava.jig.infrastructure.javaparser;

import org.dddjava.jig.domain.model.implementation.sourcecode.*;
import org.dddjava.jig.domain.model.japanese.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class JavaparserJapaneseReader implements JapaneseReader {

    private static Logger LOGGER = LoggerFactory.getLogger(JavaparserJapaneseReader.class);

    PackageInfoReader packageInfoReader = new PackageInfoReader();
    ClassReader classReader = new ClassReader();

    @Override
    public PackageNames readPackages(PackageNameSources nameSources) {
        List<PackageJapaneseName> names = new ArrayList<>();
        for (Path path : nameSources.list()) {
            packageInfoReader.read(path)
                    .ifPresent(names::add);
        }
        return new PackageNames(names);
    }

    @Override
    public TypeNames readTypes(TypeNameSources nameSources) {
        List<TypeJapaneseName> names = new ArrayList<>();
        List<MethodJapaneseName> methodNames = new ArrayList<>();

        for (Path path : nameSources.list()) {
            try {
                TypeSourceResult typeSourceResult = classReader.read(path);
                TypeJapaneseName typeJapaneseName = typeSourceResult.typeJapaneseName;
                if (typeJapaneseName != null) {
                    names.add(typeJapaneseName);
                }
                methodNames.addAll(typeSourceResult.methodJapaneseNames);
            } catch (JavaParserFailException e) {
                LOGGER.warn("Javadoc読み取りに失敗（処理続行）", e);
            }
        }
        return new TypeNames(names, methodNames);
    }
}