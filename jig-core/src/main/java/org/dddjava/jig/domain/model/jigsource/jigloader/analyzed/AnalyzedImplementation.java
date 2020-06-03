package org.dddjava.jig.domain.model.jigsource.jigloader.analyzed;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.rdbaccess.Sqls;
import org.dddjava.jig.domain.model.jigsource.file.Sources;

/**
 * 解析した実装
 */
public interface AnalyzedImplementation {

    static AnalyzedImplementation generate(Sources sources, TypeFacts typeFacts, Sqls sqls) {
        return new AnalyzedImplementationImpl(sources, typeFacts, sqls);
    }

    TypeFacts typeByteCodes();

    Sqls sqls();

    AnalyzeStatuses status();
}
