package org.dddjava.jig.domain.model.information.outbound;

import org.dddjava.jig.domain.model.data.persistence.PersistenceAccessorRepository;
import org.dddjava.jig.domain.model.information.outbound.other.OtherExternalAccessorRepository;

/**
 * 外部アクセサリポジトリの集約
 * 永続化・HTTP等、複数の外部アクセサリポジトリをまとめて渡すためのクラス。
 */
public record ExternalAccessorRepositories(
        PersistenceAccessorRepository persistenceAccessorRepository,
        OtherExternalAccessorRepository otherExternalAccessorRepository
) {
}
