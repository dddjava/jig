package org.dddjava.jig.domain.model.jigsource.jigloader;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.alias.PackageAliases;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.alias.TypeAliases;
import org.dddjava.jig.domain.model.jigsource.file.text.javacode.JavaSources;
import org.dddjava.jig.domain.model.jigsource.file.text.javacode.PackageInfoSources;

/**
 * JavaSourceから別名を読み取る
 */
public interface JavaSourceAliasReader {

    PackageAliases readPackages(PackageInfoSources nameSources);

    TypeAliases readAlias(JavaSources javaSource);
}
