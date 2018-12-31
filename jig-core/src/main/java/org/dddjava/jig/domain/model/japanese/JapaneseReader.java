package org.dddjava.jig.domain.model.japanese;

import org.dddjava.jig.domain.model.implementation.raw.PackageInfoSources;
import org.dddjava.jig.domain.model.implementation.raw.JavaSources;

/**
 * 和名読み取り機
 */
public interface JapaneseReader {

    PackageNames readPackages(PackageInfoSources packageInfoSources);

    TypeNames readTypes(JavaSources javaSources);
}
