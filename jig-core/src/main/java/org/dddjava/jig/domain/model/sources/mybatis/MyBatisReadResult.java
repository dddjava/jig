package org.dddjava.jig.domain.model.sources.mybatis;

import org.dddjava.jig.domain.model.data.persistence.PersistenceAccessorRepository;

public record MyBatisReadResult(PersistenceAccessorRepository persistenceAccessorRepository, SqlReadStatus sqlReadStatus) {

    public MyBatisReadResult(SqlReadStatus sqlReadStatus) {
        this(PersistenceAccessorRepository.empty(), sqlReadStatus);
    }

    public SqlReadStatus status() {
        if (sqlReadStatus == SqlReadStatus.成功 && persistenceAccessorRepository.isEmpty()) {
            return SqlReadStatus.SQLなし;
        }
        return sqlReadStatus;
    }
}
