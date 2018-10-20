package org.dddjava.jig.domain.model.implementation;

import org.dddjava.jig.domain.model.characteristic.CharacterizedTypeFactory;
import org.dddjava.jig.domain.model.characteristic.CharacterizedTypes;
import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCodes;
import org.dddjava.jig.domain.model.implementation.datasource.Sqls;

/**
 * プロジェクトから読み取った情報
 */
public class ProjectData {

    private final TypeByteCodes typeByteCodes;

    // データソースアクセス
    private Sqls sqls;

    // 特徴とセットになったもの
    private CharacterizedTypes characterizedTypes;

    public ProjectData(TypeByteCodes typeByteCodes, Sqls sqls, CharacterizedTypeFactory characterizedTypeFactory) {
        this.typeByteCodes = typeByteCodes;

        CharacterizedTypes characterizedTypes = new CharacterizedTypes(typeByteCodes, characterizedTypeFactory);
        this.characterizedTypes = characterizedTypes;

        this.sqls = sqls;
    }

    public Sqls sqls() {
        return sqls;
    }

    public CharacterizedTypes characterizedTypes() {
        return characterizedTypes;
    }

    public TypeByteCodes typeByteCodes() {
        return typeByteCodes;
    }
}
