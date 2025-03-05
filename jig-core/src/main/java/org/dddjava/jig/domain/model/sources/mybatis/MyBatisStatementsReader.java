package org.dddjava.jig.domain.model.sources.mybatis;

import org.dddjava.jig.domain.model.data.rdbaccess.MyBatisStatements;
import org.dddjava.jig.domain.model.data.types.JigTypeHeader;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

/**
 * SQL読み取り機
 */
public interface MyBatisStatementsReader {

    MyBatisStatements readFrom(Collection<JigTypeHeader> sources, List<Path> classPaths);
}
