package jig.infrastructure.javaparser;

import jig.domain.model.japanese.*;
import org.springframework.stereotype.Component;

@Component
public class JavaparserJapaneseReader implements JapaneseReader {

    PackageInfoReader packageInfoReader = new PackageInfoReader();
    ClassCommentReader classCommentReader = new ClassCommentReader();

    @Override
    public PackageNames readPackages(PackageNameSources nameSources) {
        return nameSources.toPackageNames(packageInfoReader::execute);
    }

    @Override
    public TypeNames readTypes(TypeNameSources nameSources) {
        return nameSources.toTypeNames(classCommentReader::execute);
    }
}