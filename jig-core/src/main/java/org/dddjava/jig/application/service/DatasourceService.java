package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.implementation.datasource.SqlReader;
import org.dddjava.jig.domain.model.implementation.datasource.SqlSources;
import org.dddjava.jig.domain.model.implementation.datasource.Sqls;
import org.springframework.stereotype.Service;

/**
 * データソースサービス
 */
@Service
public class DatasourceService {

    final SqlReader sqlReader;

    public DatasourceService(SqlReader sqlReader) {
        this.sqlReader = sqlReader;
    }

    /**
     * SQLを読み取る
     */
    public Sqls load(SqlSources sqlSources) {
        return sqlReader.readFrom(sqlSources);
    }
}
