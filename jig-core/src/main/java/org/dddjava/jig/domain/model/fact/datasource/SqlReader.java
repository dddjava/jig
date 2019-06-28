package org.dddjava.jig.domain.model.fact.datasource;

import org.dddjava.jig.domain.model.fact.source.code.sqlcode.SqlSources;

/**
 * SQL読み取り機
 */
public interface SqlReader {

    Sqls readFrom(SqlSources sources);
}
