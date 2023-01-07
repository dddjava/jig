package org.dddjava.jig.domain.model.sources.jigreader;

import org.dddjava.jig.domain.model.sources.file.text.scalacode.ScalaSources;
import org.dddjava.jig.domain.model.sources.jigfactory.ClassAndMethodComments;

/**
 * ScalaSourceから別名を読み取る
 */
public interface ScalaSourceAliasReader {

    ClassAndMethodComments readAlias(ScalaSources sources);

}
