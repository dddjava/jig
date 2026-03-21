package org.dddjava.jig.domain.model.information.external;

import org.dddjava.jig.domain.model.data.types.TypeId;

import java.util.Collection;

/**
 * 外部アクセサ（その他）
 *
 * PersistenceAccessorなどの具体的なものに分類されなかったもの。
 */
public record OtherExternalAccessor(
        TypeId typeId,
        Collection<OtherExternalAccessorOperation> operations) {
}
