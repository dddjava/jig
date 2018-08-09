package org.dddjava.jig.domain.model.declaration.type;

import java.util.List;

/**
 * 型パラメーター一覧
 */
public class TypeParameters {
    List<TypeParameter> list;

    public TypeParameters(List<TypeParameter> list) {
        this.list = list;
    }
}
