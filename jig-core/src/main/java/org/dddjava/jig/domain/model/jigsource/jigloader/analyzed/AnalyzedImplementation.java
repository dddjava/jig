package org.dddjava.jig.domain.model.jigsource.jigloader.analyzed;

import org.dddjava.jig.domain.model.jigsource.file.Sources;
import org.dddjava.jig.domain.model.parts.rdbaccess.Sqls;

import java.util.ArrayList;
import java.util.List;

public class AnalyzedImplementation {

    Sources sources;
    TypeFacts typeFacts;
    Sqls sqls;

    public AnalyzedImplementation(Sources sources, TypeFacts typeFacts, Sqls sqls) {
        this.sources = sources;
        this.typeFacts = typeFacts;
        this.sqls = sqls;
    }

    public TypeFacts typeFacts() {
        return typeFacts;
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
