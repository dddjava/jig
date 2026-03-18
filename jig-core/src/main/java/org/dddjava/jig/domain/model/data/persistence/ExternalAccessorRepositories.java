package org.dddjava.jig.domain.model.data.persistence;

/**
 * 外部アクセサリポジトリの集約
 * 永続化・HTTP等、複数の外部アクセサリポジトリをまとめて渡すためのクラス。
 */
public record ExternalAccessorRepositories(
        PersistenceAccessorRepository persistenceAccessorRepository
) {
    // 将来: HttpAccessorRepository httpAccessorRepository など追加
}
