package org.dddjava.jig.domain.model.data.persistence;

import org.dddjava.jig.domain.model.information.types.JigType;

/**
 * 永続化アクセサ
 *
 * - MyBatisの@Mapperを付与したインタフェース
 * - SpringDataJDBCのRepositoryを継承したインタフェース
 */
public record PersistenceAccessor(JigType accessorJigType, PersistenceOperations persistenceOperations) {
}
