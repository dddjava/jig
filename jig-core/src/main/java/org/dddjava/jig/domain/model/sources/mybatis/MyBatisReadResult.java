package org.dddjava.jig.domain.model.sources.mybatis;

import org.dddjava.jig.domain.model.data.rdbaccess.MyBatisStatements;

public record MyBatisReadResult(MyBatisStatements myBatisStatements, SqlReadStatus sqlReadStatus) {

    public MyBatisReadResult(SqlReadStatus sqlReadStatus) {
        this(new MyBatisStatements(), sqlReadStatus);
    }

    public SqlReadStatus status() {
        if (sqlReadStatus == SqlReadStatus.成功 && myBatisStatements.isEmpty()) {
            return SqlReadStatus.SQLなし;
        }
        return sqlReadStatus;
    }
}
