package org.dddjava.jig.domain.model.sources.jigreader;

import org.dddjava.jig.domain.model.parts.packages.PackageComments;
import org.dddjava.jig.domain.model.sources.file.text.ReadableTextSources;

/**
 * Javaのテキストソースを読み取る
 */
public interface JavaTextSourceReader {

    PackageComments readPackages(ReadableTextSources readableTextSources);

    ClassAndMethodComments readClasses(ReadableTextSources readableTextSources);
}
