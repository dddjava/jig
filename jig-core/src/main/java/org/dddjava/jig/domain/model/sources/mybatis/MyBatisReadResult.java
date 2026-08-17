package org.dddjava.jig.domain.model.sources.mybatis;

import org.dddjava.jig.domain.model.data.persistence.PersistenceAccessorRepository;
import org.dddjava.jig.domain.model.sources.ReadStatus;

import java.util.Optional;

public record MyBatisReadResult(PersistenceAccessorRepository persistenceAccessorRepository, SqlReadStatus sqlReadStatus) {

    public MyBatisReadResult(SqlReadStatus sqlReadStatus) {
        this(PersistenceAccessorRepository.empty(), sqlReadStatus);
    }

    /**
     * 記録すべき読み取り結果。記録するものがなければ空。
     */
    public Optional<ReadStatus> readStatus() {
        return status().toReadStatus();
    }

    private SqlReadStatus status() {
        if (sqlReadStatus == SqlReadStatus.成功 && persistenceAccessorRepository.isEmpty()) {
            return SqlReadStatus.SQLなし;
        }
        return sqlReadStatus;
    }
}
