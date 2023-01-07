package org.dddjava.jig.domain.model.sources.jigreader;

import org.dddjava.jig.domain.model.parts.packages.PackageComments;
import org.dddjava.jig.domain.model.sources.file.text.ReadableTextSources;
import org.dddjava.jig.domain.model.sources.jigfactory.TextSourceModel;

/**
 * Javaのテキストソースを読み取る
 */
public interface JavaTextSourceReader {

    PackageComments readPackages(ReadableTextSources readableTextSources);

    TextSourceModel readClasses(ReadableTextSources readableTextSources);
}
