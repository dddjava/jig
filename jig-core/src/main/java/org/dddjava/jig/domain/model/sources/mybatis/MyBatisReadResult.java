package org.dddjava.jig.domain.model.sources.mybatis;

import org.dddjava.jig.domain.model.data.rdbaccess.SqlStatements;

public record MyBatisReadResult(SqlStatements sqlStatements, SqlReadStatus sqlReadStatus) {

    public MyBatisReadResult(SqlReadStatus sqlReadStatus) {
        this(SqlStatements.empty(), sqlReadStatus);
    }

    public SqlReadStatus status() {
        if (sqlReadStatus == SqlReadStatus.成功 && sqlStatements.isEmpty()) {
            return SqlReadStatus.SQLなし;
        }
        return sqlReadStatus;
    }
}
