package org.dddjava.jig.domain.model.jigloaded.alias;

import org.dddjava.jig.domain.model.jigsource.source.code.javacode.JavaSources;
import org.dddjava.jig.domain.model.jigsource.source.code.javacode.PackageInfoSources;

/**
 * JavaSourceから別名を読み取る
 */
public interface JavaSourceAliasReader {

    PackageAliases readPackages(PackageInfoSources nameSources);

    TypeAliases readAlias(JavaSources javaSource);
}
