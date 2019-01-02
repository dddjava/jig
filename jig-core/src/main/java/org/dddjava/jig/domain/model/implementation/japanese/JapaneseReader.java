package org.dddjava.jig.domain.model.implementation.japanese;

import org.dddjava.jig.domain.model.implementation.raw.JavaSources;
import org.dddjava.jig.domain.model.implementation.raw.PackageInfoSources;

/**
 * 和名読み取り機
 */
public interface JapaneseReader {

    PackageNames readPackages(PackageInfoSources packageInfoSources);

    TypeNames readTypes(JavaSources javaSources);
}
