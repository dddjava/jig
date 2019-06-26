package org.dddjava.jig.domain.model.implementation.analyzed.datasource;

import org.dddjava.jig.domain.model.implementation.source.code.sqlcode.SqlSources;

/**
 * SQL読み取り機
 */
public interface SqlReader {

    Sqls readFrom(SqlSources sources);
}
