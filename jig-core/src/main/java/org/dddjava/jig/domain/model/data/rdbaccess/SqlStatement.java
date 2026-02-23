package org.dddjava.jig.domain.model.data.rdbaccess;

import java.util.Optional;

/**
 * SQL
 */
public record SqlStatement(SqlStatementId sqlStatementId, Query query, SqlType sqlType, Optional<Tables> resolvedTables) {

    public SqlStatement(SqlStatementId sqlStatementId, Query query, SqlType sqlType) {
        this(sqlStatementId, query, sqlType, Optional.<Tables>empty());
    }

    public SqlStatement(SqlStatementId sqlStatementId, Query query, SqlType sqlType, Tables resolvedTables) {
        this(sqlStatementId, query, sqlType, Optional.of(resolvedTables));
    }

    public Tables tables() {
        if (resolvedTables.isPresent()) return resolvedTables.get();

        if (query.supported()) {
            Table table = sqlType.extractTable(query.normalizedQuery(), sqlStatementId);
            return new Tables(table);
        }
        return new Tables(sqlType.unexpectedTable());
    }
}
