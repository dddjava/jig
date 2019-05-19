package org.dddjava.jig.domain.model.implementation.analyzed.alias;

import org.dddjava.jig.domain.model.implementation.raw.PackageInfoSources;
import org.dddjava.jig.domain.model.implementation.raw.SourceCode;
import org.dddjava.jig.domain.model.implementation.raw.SourceCodes;

/**
 * 別名読み取り機
 */
public interface AliasReader {

    PackageNames readPackages(PackageInfoSources packageInfoSources);

    TypeNames readTypes(SourceCodes<? extends SourceCode> sourceCodes);
}
