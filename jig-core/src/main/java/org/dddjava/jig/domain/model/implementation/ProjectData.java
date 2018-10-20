package org.dddjava.jig.domain.model.implementation;

import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCodes;
import org.dddjava.jig.domain.model.implementation.datasource.Sqls;

/**
 * プロジェクトから読み取った情報
 */
public class ProjectData {

    private final TypeByteCodes typeByteCodes;

    // データソースアクセス
    private Sqls sqls;

    public ProjectData(TypeByteCodes typeByteCodes, Sqls sqls) {
        this.typeByteCodes = typeByteCodes;
        this.sqls = sqls;
    }

    public Sqls sqls() {
        return sqls;
    }

    public TypeByteCodes typeByteCodes() {
        return typeByteCodes;
    }
}
