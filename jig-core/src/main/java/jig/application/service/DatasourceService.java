package jig.application.service;

import jig.domain.model.datasource.SqlReader;
import jig.domain.model.datasource.SqlRepository;
import jig.domain.model.datasource.SqlSources;
import jig.domain.model.datasource.Sqls;
import org.springframework.stereotype.Service;

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
}
