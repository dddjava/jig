package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.implementation.datasource.SqlReader;
import org.dddjava.jig.domain.model.implementation.datasource.SqlRepository;
import org.dddjava.jig.domain.model.implementation.datasource.SqlSources;
import org.dddjava.jig.domain.model.implementation.datasource.Sqls;
import org.springframework.stereotype.Service;

/**
 * データソースサービス
 */
@Service
public class DatasourceService {

    final SqlReader sqlReader;
    final SqlRepository sqlRepository;

    public DatasourceService(SqlReader sqlReader, SqlRepository sqlRepository) {
        this.sqlReader = sqlReader;
        this.sqlRepository = sqlRepository;
    }

    public void importDatabaseAccess(SqlSources sqlSources) {
        Sqls sqls = sqlReader.readFrom(sqlSources);
        sqlRepository.register(sqls);
    }

    public Sqls allSqls() {
        return sqlRepository.all();
    }
}
