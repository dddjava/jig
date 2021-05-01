package org.dddjava.jig.domain.model.jigsource.jigreader;

import org.dddjava.jig.domain.model.jigsource.file.text.kotlincode.KotlinSources;

/**
 * KotlinSourceから別名を読み取る
 */
public interface KotlinSourceAliasReader {

    ClassAndMethodComments readAlias(KotlinSources kotlinSources);
}
