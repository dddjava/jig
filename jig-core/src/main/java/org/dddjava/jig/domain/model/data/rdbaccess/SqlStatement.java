package org.dddjava.jig.domain.model.data.rdbaccess;

import java.util.Optional;

/**
 * SQL
 */
public record SqlStatement(SqlStatementId sqlStatementId, Query query, SqlType sqlType, Optional<Table> resolvedTable) {

    public SqlStatement(SqlStatementId sqlStatementId, Query query, SqlType sqlType) {
        this(sqlStatementId, query, sqlType, Optional.empty());
    }

    public SqlStatement(SqlStatementId sqlStatementId, Query query, SqlType sqlType, Table resolvedTable) {
        this(sqlStatementId, query, sqlType, Optional.of(resolvedTable));
    }

    public Tables tables() {
        if (resolvedTable.isPresent()) return new Tables(resolvedTable.get());

        if (query.supported()) {
            Table table = sqlType.extractTable(query.normalizedQuery(), sqlStatementId);
            return new Tables(table);
        }
        return new Tables(sqlType.unexpectedTable());
    }
}
