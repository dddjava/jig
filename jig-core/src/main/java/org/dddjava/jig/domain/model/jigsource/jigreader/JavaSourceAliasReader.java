package org.dddjava.jig.domain.model.jigsource.jigreader;

import org.dddjava.jig.domain.model.jigsource.file.text.javacode.JavaSources;
import org.dddjava.jig.domain.model.jigsource.file.text.javacode.PackageInfoSources;
import org.dddjava.jig.domain.model.parts.package_.PackageComments;

/**
 * JavaSourceから別名を読み取る
 */
public interface JavaSourceAliasReader {

    PackageComments readPackages(PackageInfoSources nameSources);

    ClassAndMethodComments readAlias(JavaSources javaSource);
}
