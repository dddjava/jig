package org.dddjava.jig.domain.model.jigsource.jigreader;

import org.dddjava.jig.domain.model.jigsource.file.text.scalacode.ScalaSources;

/**
 * ScalaSourceから別名を読み取る
 */
public interface ScalaSourceAliasReader {

    ClassAndMethodComments readAlias(ScalaSources sources);

}
