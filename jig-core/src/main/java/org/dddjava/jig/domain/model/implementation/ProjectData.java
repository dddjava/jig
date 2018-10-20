package org.dddjava.jig.domain.model.implementation;

import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCodes;

/**
 * プロジェクトから読み取った情報
 */
public class ProjectData {

    private final TypeByteCodes typeByteCodes;

    public ProjectData(TypeByteCodes typeByteCodes) {
        this.typeByteCodes = typeByteCodes;
    }

    public TypeByteCodes typeByteCodes() {
        return typeByteCodes;
    }
}
