package org.dddjava.jig.domain.model.implementation;

import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCodes;
import org.dddjava.jig.domain.model.implementation.datasource.Sqls;
import org.dddjava.jig.domain.model.implementation.raw.RawSource;

import java.util.ArrayList;
import java.util.List;

public class Implementations {

    RawSource rawSource;
    TypeByteCodes typeByteCodes;
    Sqls sqls;

    public Implementations(RawSource rawSource, TypeByteCodes typeByteCodes, Sqls sqls) {
        this.rawSource = rawSource;
        this.typeByteCodes = typeByteCodes;
        this.sqls = sqls;
    }

    public TypeByteCodes typeByteCodes() {
        return typeByteCodes;
    }

    public Sqls sqls() {
        return sqls;
    }

    public ImplementationStatuses status() {
        List<ImplementationStatus> list = new ArrayList<>();

        if (rawSource.nothingBinarySource()) {
            list.add(ImplementationStatus.バイナリソースなし);
        }

        if (rawSource.nothingTextSource()) {
            list.add(ImplementationStatus.テキストソースなし);
        }

        // binarySourceがあってtypeByteCodesがない（ASMの解析で失敗する）のは現状実行時エラーになるのでここでは考慮しない

        if (sqls.status().not正常()) {
            list.add(ImplementationStatus.fromSqlReadStatus(sqls.status()));
        }

        return new ImplementationStatuses(list);
    }
}
