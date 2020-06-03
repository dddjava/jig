package org.dddjava.jig.domain.model.jigsource.jigloader.analyzed;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.rdbaccess.Sqls;
import org.dddjava.jig.domain.model.jigsource.file.Sources;

import java.util.ArrayList;
import java.util.List;

public class AnalyzedImplementationImpl implements AnalyzedImplementation {

    Sources sources;
    TypeFacts typeFacts;
    Sqls sqls;

    AnalyzedImplementationImpl(Sources sources, TypeFacts typeFacts, Sqls sqls) {
        this.sources = sources;
        this.typeFacts = typeFacts;
        this.sqls = sqls;
    }

    @Override
    public TypeFacts typeByteCodes() {
        return typeFacts;
    }

    @Override
    public Sqls sqls() {
        return sqls;
    }

    @Override
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
