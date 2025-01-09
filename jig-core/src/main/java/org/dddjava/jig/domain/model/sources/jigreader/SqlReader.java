package org.dddjava.jig.domain.model.sources.jigreader;

import org.dddjava.jig.domain.model.data.classes.rdbaccess.MyBatisStatements;
import org.dddjava.jig.domain.model.sources.file.text.sqlcode.SqlSources;

/**
 * SQL読み取り機
 */
public interface SqlReader {

    MyBatisStatements readFrom(SqlSources sources);
}
