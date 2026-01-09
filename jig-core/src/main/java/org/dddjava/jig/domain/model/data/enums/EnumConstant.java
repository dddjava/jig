package org.dddjava.jig.domain.model.data.enums;

import java.util.List;

/**
 * Enumの定数
 *
 * 定数名とコンストラクタの引数リストを持つ
 *
 * @param name                定数名
 * @param argumentExpressions 引数式（実装されているままのコード）のリスト
 */
public record EnumConstant(String name, List<String> argumentExpressions) {

}
