package org.dddjava.jig.domain.model.sources.mybatis;

import org.dddjava.jig.domain.model.data.persistence.PersistenceOperationsRepository;

public record MyBatisReadResult(PersistenceOperationsRepository persistenceOperationsRepository, SqlReadStatus sqlReadStatus) {

    public MyBatisReadResult(SqlReadStatus sqlReadStatus) {
        this(PersistenceOperationsRepository.empty(), sqlReadStatus);
    }

    public SqlReadStatus status() {
        if (sqlReadStatus == SqlReadStatus.成功 && persistenceOperationsRepository.isEmpty()) {
            return SqlReadStatus.SQLなし;
        }
        return sqlReadStatus;
    }
}
