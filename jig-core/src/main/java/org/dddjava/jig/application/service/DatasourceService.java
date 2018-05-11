package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.implementation.datasource.*;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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

    public Sqls findSqls(MethodDeclarations mapperMethods) {
        List<Sql> sqls = new ArrayList<>();
        for (MethodDeclaration identifier : mapperMethods.list()) {
            sqlRepository.find(identifier).ifPresent(sqls::add);
        }
        return new Sqls(sqls);
    }
}
