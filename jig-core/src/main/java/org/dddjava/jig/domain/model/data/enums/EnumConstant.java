package org.dddjava.jig.domain.model.data.enums;

import java.util.List;

/**
 * Enumの定数
 *
 * 定数名とコンストラクタの引数リストを持つ
 */
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
