package org.dddjava.jig.domain.model.jigloaded.analyzed;

import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCodes;
import org.dddjava.jig.domain.model.implementation.datasource.Sqls;
import org.dddjava.jig.domain.model.implementation.source.Sources;

import java.util.ArrayList;
import java.util.List;

/**
 * 解析した実装
 */
public class AnalyzedImplementation {

    Sources sources;
    TypeByteCodes typeByteCodes;
    Sqls sqls;

    public AnalyzedImplementation(Sources sources, TypeByteCodes typeByteCodes, Sqls sqls) {
        this.sources = sources;
        this.typeByteCodes = typeByteCodes;
        this.sqls = sqls;
    }

    public TypeByteCodes typeByteCodes() {
        return typeByteCodes;
    }

    public Sqls sqls() {
        return sqls;
    }

    public AnalyzeStatuses status() {
        List<AnalyzeStatus> list = new ArrayList<>();

        if (sources.nothingBinarySource()) {
            list.add(AnalyzeStatus.バイナリソースなし);
        }

        if (sources.nothingTextSource()) {
            list.add(AnalyzeStatus.テキストソースなし);
        }

        // binarySourceがあってtypeByteCodesがない（ASMの解析で失敗する）のは現状実行時エラーになるのでここでは考慮しない

        if (sqls.status().not正常()) {
            list.add(AnalyzeStatus.fromSqlReadStatus(sqls.status()));
        }

        return new AnalyzeStatuses(list);
    }
}
