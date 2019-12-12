package org.dddjava.jig.domain.model.jigsource.datasource;

import org.dddjava.jig.domain.model.jigsource.source.code.sqlcode.SqlSources;

/**
 * SQL読み取り機
 */
public interface SqlReader {

    Sqls readFrom(SqlSources sources);
}
