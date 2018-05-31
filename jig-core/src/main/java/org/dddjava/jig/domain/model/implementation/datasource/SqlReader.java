package org.dddjava.jig.domain.model.implementation.datasource;

/**
 * SQL読み取り機
 */
public interface SqlReader {

    Sqls readFrom(SqlSources sources);
}
