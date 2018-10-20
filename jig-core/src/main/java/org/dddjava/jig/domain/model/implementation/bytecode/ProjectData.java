package org.dddjava.jig.domain.model.implementation.bytecode;

/**
 * プロジェクトから読み取った情報
 */
public class ProjectData extends TypeByteCodes {

    public ProjectData(ProjectData typeByteCodes) {
        super(typeByteCodes.list());
    }
}
