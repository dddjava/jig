package org.dddjava.jig.domain.model.jigsource.jigloader;

import org.dddjava.jig.domain.model.jigsource.file.binary.ClassSources;
import org.dddjava.jig.domain.model.jigsource.jigloader.analyzed.TypeFacts;

/**
 * 対象から実装を取得するファクトリ
 */
public interface FactReader {

    TypeFacts readTypeFacts(ClassSources classSources);
}
