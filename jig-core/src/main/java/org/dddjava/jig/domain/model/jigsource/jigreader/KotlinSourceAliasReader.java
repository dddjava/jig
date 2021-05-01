package org.dddjava.jig.domain.model.jigsource.jigreader;

import org.dddjava.jig.domain.model.jigsource.file.text.kotlincode.KotlinSources;
import org.dddjava.jig.domain.model.parts.class_.type.ClassComments;

/**
 * KotlinSourceから別名を読み取る
 */
public interface KotlinSourceAliasReader {

    ClassComments readAlias(KotlinSources kotlinSources);
}
