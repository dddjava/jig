package org.dddjava.jig.domain.model.japanese;

public interface JapaneseReader {

    PackageNames readPackages(PackageNameSources packageNameSources);

    TypeNames readTypes(TypeNameSources typeNameSources);
}
