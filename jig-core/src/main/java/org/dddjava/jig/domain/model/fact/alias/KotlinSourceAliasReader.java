package org.dddjava.jig.domain.model.fact.alias;

import org.dddjava.jig.domain.model.fact.source.code.kotlincode.KotlinSources;

/**
 * KotlinSourceから別名を読み取る
 */
public interface KotlinSourceAliasReader {

    TypeAliases readAlias(KotlinSources kotlinSources);
}
