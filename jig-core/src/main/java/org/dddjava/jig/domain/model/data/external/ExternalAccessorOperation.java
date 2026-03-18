package org.dddjava.jig.domain.model.data.external;

import org.dddjava.jig.domain.model.data.types.TypeId;

/**
 * 外部アクセサ
 * JIG読み取り範囲外のクラスをフィールドとして持ち、そのメソッドを呼び出しているクラスを表す。
 * 1インスタンス = 1呼び出し関係（アクセッサメソッド → 外部型メソッド）。
 */
public record ExternalAccessorOperation(
        TypeId accessorTypeId,
        String accessorMethodName,
        TypeId externalTypeId,
        String externalMethodName
) {
}
