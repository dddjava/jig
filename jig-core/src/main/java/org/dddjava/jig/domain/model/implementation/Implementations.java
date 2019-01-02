package org.dddjava.jig.domain.model.implementation;

import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCodes;
import org.dddjava.jig.domain.model.implementation.datasource.Sqls;

public class Implementations {

    TypeByteCodes typeByteCodes;
    Sqls sqls;

    public Implementations(TypeByteCodes typeByteCodes, Sqls sqls) {
        this.typeByteCodes = typeByteCodes;
        this.sqls = sqls;
    }

    public TypeByteCodes typeByteCodes() {
        return typeByteCodes;
    }

    public Sqls sqls() {
        return sqls;
    }
}
