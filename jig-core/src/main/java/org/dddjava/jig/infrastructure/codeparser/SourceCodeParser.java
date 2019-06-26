package org.dddjava.jig.infrastructure.codeparser;

import org.dddjava.jig.domain.model.implementation.analyzed.alias.PackageAliases;
import org.dddjava.jig.domain.model.implementation.analyzed.alias.TypeAliases;
import org.dddjava.jig.domain.model.implementation.raw.SourceCode;
import org.dddjava.jig.domain.model.implementation.raw.SourceCodes;
import org.dddjava.jig.domain.model.implementation.raw.packageinfo.PackageInfoSources;

/**
 * ソースコードのパーサー
 */
public interface SourceCodeParser {

    boolean isSupport(PackageInfoSources packageInfoSources);

    boolean isSupport(SourceCodes<? extends SourceCode> sourceCodes);

    PackageAliases readPackages(PackageInfoSources packageInfoSources);

    TypeAliases readTypes(SourceCodes<? extends SourceCode> sourceCodes);
}
