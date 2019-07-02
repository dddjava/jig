package org.dddjava.jig.domain.model.interpret.alias;

import org.dddjava.jig.domain.model.implementation.source.code.kotlincode.KotlinSources;

/**
 * KotlinSourceから別名を読み取る
 */
public interface KotlinSourceAliasReader {

    TypeAliases readAlias(KotlinSources kotlinSources);
}
