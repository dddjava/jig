package org.dddjava.jig.domain.model.sources.jigreader;

import org.dddjava.jig.domain.model.sources.file.text.kotlincode.KotlinSources;

/**
 * Kotlinのテキストソースを読み取る
 */
public interface KotlinTextSourceReader {

    ClassAndMethodComments readClasses(KotlinSources kotlinSources);
}
