package org.dddjava.jig.domain.model.sources.jigreader;

import org.dddjava.jig.domain.model.parts.package_.PackageComments;
import org.dddjava.jig.domain.model.sources.file.text.javacode.JavaSources;
import org.dddjava.jig.domain.model.sources.file.text.javacode.PackageInfoSources;

/**
 * JavaSourceから別名を読み取る
 */
public interface JavaSourceAliasReader {

    PackageComments readPackages(PackageInfoSources nameSources);

    ClassAndMethodComments readAlias(JavaSources javaSource);
}
