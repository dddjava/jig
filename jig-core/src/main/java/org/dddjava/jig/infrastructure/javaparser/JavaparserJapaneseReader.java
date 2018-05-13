package org.dddjava.jig.infrastructure.javaparser;

import org.dddjava.jig.domain.model.implementation.sourcecode.*;
import org.dddjava.jig.domain.model.japanese.MethodJapaneseName;
import org.dddjava.jig.domain.model.japanese.PackageJapaneseName;
import org.dddjava.jig.domain.model.japanese.TypeJapaneseName;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
public class JavaparserJapaneseReader implements JapaneseReader {

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
            TypeSourceResult typeSourceResult = classReader.read(path);
            TypeJapaneseName typeJapaneseName = typeSourceResult.typeJapaneseName;
            if (typeJapaneseName != null) {
                names.add(typeJapaneseName);
            }
            methodNames.addAll(typeSourceResult.methodJapaneseNames);
        }
        return new TypeNames(names, methodNames);
    }
}