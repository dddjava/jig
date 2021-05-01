package org.dddjava.jig.domain.model.sources.jigreader;

import org.dddjava.jig.domain.model.sources.file.text.kotlincode.KotlinSources;

/**
 * KotlinSourceから別名を読み取る
 */
public interface KotlinSourceAliasReader {

    ClassAndMethodComments readAlias(KotlinSources kotlinSources);
}
