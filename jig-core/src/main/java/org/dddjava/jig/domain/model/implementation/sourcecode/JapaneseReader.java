package org.dddjava.jig.domain.model.implementation.sourcecode;

import org.dddjava.jig.domain.model.japanese.PackageNames;
import org.dddjava.jig.domain.model.japanese.TypeNames;

/**
 * 和名読み取り機
 */
public interface JapaneseReader {

    PackageNames readPackages(PackageNameSources packageNameSources);

    TypeNames readTypes(TypeNameSources typeNameSources);
}
