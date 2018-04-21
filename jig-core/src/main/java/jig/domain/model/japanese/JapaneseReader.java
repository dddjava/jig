package jig.domain.model.japanese;

public interface JapaneseReader {

    PackageNames readPackages(PackageNameSources nameSources);

    TypeNames readTypes(TypeNameSources nameSources);
}
