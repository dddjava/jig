package jig.domain.model.datasource;

import jig.infrastructure.mybatis.SqlSources;

public interface SqlReader {

    Sqls readFrom(SqlSources sources);
}
