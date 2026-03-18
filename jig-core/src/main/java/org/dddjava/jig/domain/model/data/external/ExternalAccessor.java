package org.dddjava.jig.domain.model.data.external;

import org.dddjava.jig.domain.model.data.types.TypeId;

/**
 * 外部アクセサ
 * JIG読み取り範囲外のクラスをフィールドとして持ち、そのメソッドを呼び出しているクラスを表す。
 */
public record ExternalAccessor(
        TypeId accessorTypeId,
        TypeId externalTypeId
) {}
