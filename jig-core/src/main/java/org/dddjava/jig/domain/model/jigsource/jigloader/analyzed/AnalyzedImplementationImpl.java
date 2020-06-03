package org.dddjava.jig.domain.model.jigsource.jigloader.analyzed;

import org.dddjava.jig.domain.model.jigmodel.datasource.Sqls;
import org.dddjava.jig.domain.model.jigsource.file.Sources;
import org.dddjava.jig.domain.model.jigsource.file.binary.TypeByteCodes;

import java.util.ArrayList;
import java.util.List;

public class AnalyzedImplementationImpl implements AnalyzedImplementation {

    Sources sources;
    TypeByteCodes typeByteCodes;
    Sqls sqls;

    AnalyzedImplementationImpl(Sources sources, TypeByteCodes typeByteCodes, Sqls sqls) {
        this.sources = sources;
        this.typeByteCodes = typeByteCodes;
        this.sqls = sqls;
    }

    @Override
    public TypeByteCodes typeByteCodes() {
        return typeByteCodes;
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
