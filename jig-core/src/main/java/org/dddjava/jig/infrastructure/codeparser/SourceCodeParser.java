package org.dddjava.jig.infrastructure.codeparser;

import org.dddjava.jig.domain.model.implementation.analyzed.alias.PackageNames;
import org.dddjava.jig.domain.model.implementation.analyzed.alias.TypeNames;
import org.dddjava.jig.domain.model.implementation.raw.SourceCode;
import org.dddjava.jig.domain.model.implementation.raw.SourceCodes;
import org.dddjava.jig.domain.model.implementation.raw.packageinfo.PackageInfoSources;

/**
 * ソースコードのパーサー
 */
public interface SourceCodeParser {

    boolean isSupport(PackageInfoSources packageInfoSources);

    boolean isSupport(SourceCodes<? extends SourceCode> sourceCodes);

    PackageNames readPackages(PackageInfoSources packageInfoSources);

    TypeNames readTypes(SourceCodes<? extends SourceCode> sourceCodes);
}
