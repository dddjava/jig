package org.dddjava.jig.domain.model.jigsource.jigreader;

import org.dddjava.jig.domain.model.jigsource.file.binary.ClassSources;
import org.dddjava.jig.domain.model.jigsource.jigfactory.TypeFacts;

/**
 * 対象から実装を取得するファクトリ
 */
public interface FactReader {

    TypeFacts readTypeFacts(ClassSources classSources);
}
