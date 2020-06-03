package org.dddjava.jig.domain.model.jigsource.jigloader.analyzed;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.rdbaccess.Sqls;
import org.dddjava.jig.domain.model.jigsource.file.Sources;

/**
 * 解析した実装
 */
public interface AnalyzedImplementation {

    static AnalyzedImplementation generate(Sources sources, TypeByteCodes typeByteCodes, Sqls sqls) {
        return new AnalyzedImplementationImpl(sources, typeByteCodes, sqls);
    }

    TypeByteCodes typeByteCodes();

    Sqls sqls();

    AnalyzeStatuses status();
}
