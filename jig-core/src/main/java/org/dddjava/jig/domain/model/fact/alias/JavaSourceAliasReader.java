package org.dddjava.jig.domain.model.fact.alias;

import org.dddjava.jig.domain.model.fact.source.code.javacode.JavaSources;
import org.dddjava.jig.domain.model.fact.source.code.javacode.PackageInfoSources;

/**
 * JavaSourceから別名を読み取る
 */
public interface JavaSourceAliasReader {

    PackageAliases readPackages(PackageInfoSources nameSources);

    TypeAliases readAlias(JavaSources javaSource);
}
