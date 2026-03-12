package org.dddjava.jig.domain.model.sources.mybatis;

import org.dddjava.jig.domain.model.data.persistence.PersistenceAccessorsRepository;

public record MyBatisReadResult(PersistenceAccessorsRepository persistenceAccessorsRepository, SqlReadStatus sqlReadStatus) {

    public MyBatisReadResult(SqlReadStatus sqlReadStatus) {
        this(PersistenceAccessorsRepository.empty(), sqlReadStatus);
    }

    public SqlReadStatus status() {
        if (sqlReadStatus == SqlReadStatus.成功 && persistenceAccessorsRepository.isEmpty()) {
            return SqlReadStatus.SQLなし;
        }
        return sqlReadStatus;
    }
}
