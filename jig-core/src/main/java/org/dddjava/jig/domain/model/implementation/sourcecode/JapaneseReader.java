package org.dddjava.jig.domain.model.implementation.sourcecode;

/**
 * 和名読み取り機
 */
public interface JapaneseReader {

    PackageNames readPackages(PackageNameSources packageNameSources);

    TypeNames readTypes(TypeNameSources typeNameSources);
}
