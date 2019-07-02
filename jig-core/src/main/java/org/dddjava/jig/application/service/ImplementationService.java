package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.implementation.bytecode.ByteCodeFactory;
import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCodes;
import org.dddjava.jig.domain.model.implementation.datasource.SqlReader;
import org.dddjava.jig.domain.model.implementation.datasource.Sqls;
import org.dddjava.jig.domain.model.implementation.source.SourcePaths;
import org.dddjava.jig.domain.model.implementation.source.SourceReader;
import org.dddjava.jig.domain.model.implementation.source.Sources;
import org.dddjava.jig.domain.model.implementation.source.binary.ClassSources;
import org.dddjava.jig.domain.model.implementation.source.code.sqlcode.SqlSources;
import org.dddjava.jig.domain.model.interpret.analyzed.AnalyzedImplementation;
import org.springframework.stereotype.Service;

/**
 * 取り込みサービス
 */
@Service
public class ImplementationService {

    AliasService aliasService;

    ByteCodeFactory byteCodeFactory;
    SqlReader sqlReader;

    SourceReader sourceReader;

    public ImplementationService(ByteCodeFactory byteCodeFactory, AliasService aliasService, SqlReader sqlReader, SourceReader sourceReader) {
        this.byteCodeFactory = byteCodeFactory;
        this.aliasService = aliasService;
        this.sqlReader = sqlReader;
        this.sourceReader = sourceReader;
    }

    public AnalyzedImplementation implementations(SourcePaths sourcePaths) {
        Sources source = sourceReader.readSources(sourcePaths);

        TypeByteCodes typeByteCodes = readProjectData(source);
        Sqls sqls = readSql(source.sqlSources());

        return new AnalyzedImplementation(source, typeByteCodes, sqls);
    }

    /**
     * プロジェクト情報を読み取る
     */
    public TypeByteCodes readProjectData(Sources sources) {
        TypeByteCodes typeByteCodes = readByteCode(sources.classSources());

        aliasService.loadAliases(sources.aliasSource());

        return typeByteCodes;
    }

    /**
     * ソースからバイトコードを読み取る
     */
    public TypeByteCodes readByteCode(ClassSources classSources) {
        return byteCodeFactory.readFrom(classSources);
    }

    /**
     * ソースからSQLを読み取る
     */
    public Sqls readSql(SqlSources sqlSources) {
        return sqlReader.readFrom(sqlSources);
    }
}
