package org.dddjava.jig.domain.model.data.rdbaccess;

import java.util.Collection;

/**
 * DBアクセスグループ
 *
 * namespace単位でまとめたもの。
 * 通常namespaceはtypeId
 */
public record SqlStatementGroup(
        String namespace,
        Collection<SqlStatement> sqlStatements
) {
}
