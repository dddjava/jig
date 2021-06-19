package org.dddjava.jig.domain.model.sources.jigreader;

import org.dddjava.jig.domain.model.parts.packages.PackageComments;
import org.dddjava.jig.domain.model.sources.file.text.javacode.JavaSources;
import org.dddjava.jig.domain.model.sources.file.text.javacode.PackageInfoSources;

/**
 * Javaのテキストソースを読み取る
 */
public interface JavaTextSourceReader {

    PackageComments readPackages(PackageInfoSources nameSources);

    ClassAndMethodComments readClasses(JavaSources javaSource);
}
