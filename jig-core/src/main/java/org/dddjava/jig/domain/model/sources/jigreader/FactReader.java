package org.dddjava.jig.domain.model.sources.jigreader;

import org.dddjava.jig.domain.model.sources.file.binary.ClassSources;
import org.dddjava.jig.domain.model.sources.jigfactory.TextSourceModel;
import org.dddjava.jig.domain.model.sources.jigfactory.TypeFacts;

/**
 * 対象から実装を取得するファクトリ
 */
public interface FactReader {

    TypeFacts readTypeFacts(ClassSources classSources, TextSourceModel textSourceModel);
}
