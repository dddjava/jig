package org.dddjava.jig.domain.model.implementation.analyzed.alias;

import org.dddjava.jig.domain.model.implementation.source.code.kotlincode.KotlinSources;

public interface KotlinSourceAliasReader {

    TypeAliases readAlias(KotlinSources kotlinSources);
}
