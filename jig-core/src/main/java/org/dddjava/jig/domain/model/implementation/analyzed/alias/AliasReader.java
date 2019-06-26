package org.dddjava.jig.domain.model.implementation.analyzed.alias;

import org.dddjava.jig.domain.model.implementation.raw.SourceCode;
import org.dddjava.jig.domain.model.implementation.raw.SourceCodes;
import org.dddjava.jig.domain.model.implementation.source.code.javacode.PackageInfoSources;

/**
 * 別名読み取り機
 */
public interface AliasReader {

    PackageAliases readPackages(PackageInfoSources packageInfoSources);

    TypeAliases readTypes(SourceCodes<? extends SourceCode> sourceCodes);
}
