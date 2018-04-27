package org.dddjava.jig.infrastructure.javaparser;

import org.dddjava.jig.domain.model.japanese.*;
import org.dddjava.jig.domain.model.japanese.PackageNameSources;
import org.dddjava.jig.domain.model.japanese.TypeNameSources;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
public class JavaparserJapaneseReader implements JapaneseReader {

    PackageInfoReader packageInfoReader = new PackageInfoReader();
    ClassCommentReader classCommentReader = new ClassCommentReader();

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
        for (Path path : nameSources.list()) {
            classCommentReader.read(path)
                    .ifPresent(names::add);
        }
        return new TypeNames(names);
    }
}