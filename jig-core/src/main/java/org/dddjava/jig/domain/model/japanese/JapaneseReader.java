package org.dddjava.jig.domain.model.japanese;

import org.dddjava.jig.domain.model.implementation.sourcecode.PackageNameSources;
import org.dddjava.jig.domain.model.implementation.sourcecode.TypeNameSources;

/**
 * 和名読み取り機
 */
public interface JapaneseReader {

    PackageNames readPackages(PackageNameSources packageNameSources);

    TypeNames readTypes(TypeNameSources typeNameSources);
}
