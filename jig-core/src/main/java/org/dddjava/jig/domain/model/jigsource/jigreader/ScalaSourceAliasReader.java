package org.dddjava.jig.domain.model.jigsource.jigreader;

import org.dddjava.jig.domain.model.jigsource.file.text.scalacode.ScalaSources;
import org.dddjava.jig.domain.model.parts.class_.type.ClassComments;

/**
 * ScalaSourceから別名を読み取る
 */
public interface ScalaSourceAliasReader {

    ClassComments readAlias(ScalaSources sources);

}
