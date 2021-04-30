package org.dddjava.jig.domain.model.jigsource.jigloader;

import org.dddjava.jig.domain.model.jigsource.file.text.scalacode.ScalaSources;
import org.dddjava.jig.domain.model.parts.alias.TypeAliases;

/**
 * ScalaSourceから別名を読み取る
 */
public interface ScalaSourceAliasReader {

    TypeAliases readAlias(ScalaSources sources);

}
