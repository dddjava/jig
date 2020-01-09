package org.dddjava.jig.domain.model.jigloaded.alias;

import org.dddjava.jig.domain.model.jigsource.source.code.scalacode.ScalaSources;

/**
 * ScalaSourceから別名を読み取る
 */
public interface ScalaSourceAliasReader {

    TypeAliases readAlias(ScalaSources sources);

}
