package org.dddjava.jig.domain.model.models.domains.categories.enums;

import java.util.List;

public class EnumConstant {
    /**
     * 定数名
     */
    String name;
    /**
     * 引数リスト
     * 実装されているまま持っておく
     */
    List<String> argumentExpressions;

    public EnumConstant(String name, List<String> argumentExpressions) {
        this.name = name;
        this.argumentExpressions = argumentExpressions;
    }
}
