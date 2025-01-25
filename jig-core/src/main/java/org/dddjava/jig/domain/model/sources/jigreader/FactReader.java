package org.dddjava.jig.domain.model.sources.jigreader;

import org.dddjava.jig.domain.model.sources.file.binary.ClassSources;
import org.dddjava.jig.domain.model.sources.jigfactory.ClassSourceModel;

/**
 * 対象から実装を取得するファクトリ
 */
public interface FactReader {

    ClassSourceModel byteSourceModel(ClassSources classSources);
}
