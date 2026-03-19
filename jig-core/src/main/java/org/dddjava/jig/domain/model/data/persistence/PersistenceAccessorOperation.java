package org.dddjava.jig.domain.model.data.persistence;

import java.util.Collection;

/**
 * 永続化アクセサ操作
 *
 * @param operations             永続化対象ごとにどのような操作を実行するかを表す
 * @param statementOperationType この操作としての永続化操作の種類。SQLとして何文かに対応。
 * @param query                  参考情報。クエリが存在する場合のみ持つ
 */
public record PersistenceAccessorOperation(PersistenceAccessorOperationId id,
                                           PersistenceOperationType statementOperationType,
                                           PersistenceTargetOperationTypes operations,
                                           Query query
) {

    public static PersistenceAccessorOperation from(PersistenceAccessorOperationId id, PersistenceOperationType statementOperationType, Query query) {
        return new PersistenceAccessorOperation(id, statementOperationType,
                statementOperationType.extractTable(query, id),
                query);
    }

    public static PersistenceAccessorOperation from(PersistenceAccessorOperationId id, PersistenceOperationType statementOperationType, Collection<PersistenceTarget> persistenceTargets) {
        return new PersistenceAccessorOperation(id, statementOperationType,
                new PersistenceTargetOperationTypes(persistenceTargets.stream()
                        .map(persistenceTarget -> PersistenceTargetOperationType.from(persistenceTarget, statementOperationType))
                        .toList()),
                // TODO: Queryはunsupportedではなくauto-generateとかそんな感じかと思う
                Query.unsupported()
        );
    }
}
