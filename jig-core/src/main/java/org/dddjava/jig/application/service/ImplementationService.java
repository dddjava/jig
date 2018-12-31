package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.implementation.bytecode.ByteCodeFactory;
import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCodes;
import org.dddjava.jig.domain.model.implementation.datasource.SqlReader;
import org.dddjava.jig.domain.model.implementation.datasource.SqlSources;
import org.dddjava.jig.domain.model.implementation.datasource.Sqls;
import org.dddjava.jig.domain.model.implementation.raw.ClassSources;
import org.dddjava.jig.domain.model.implementation.raw.NotCompiledSources;
import org.dddjava.jig.infrastructure.LocalProject;
import org.springframework.stereotype.Service;

/**
 * 取り込みサービス
 */
@Service
public class ImplementationService {

    final GlossaryService glossaryService;

    final ByteCodeFactory byteCodeFactory;
    final SqlReader sqlReader;

    public ImplementationService(ByteCodeFactory byteCodeFactory, GlossaryService glossaryService, SqlReader sqlReader) {
        this.byteCodeFactory = byteCodeFactory;
        this.glossaryService = glossaryService;
        this.sqlReader = sqlReader;
    }

    /**
     * プロジェクト情報を読み取る
     */
    public TypeByteCodes readProjectData(LocalProject target) {
        TypeByteCodes typeByteCodes = readByteCode(target.getByteCodeSources());

        NotCompiledSources notCompiledSources = target.notCompiledSources();
        glossaryService.importJapanese(notCompiledSources.javaSources());
        glossaryService.importJapanese(notCompiledSources.packageInfoSources());

        return typeByteCodes;
    }

    /**
     * ソースからバイトコードを読み取る
     */
    public TypeByteCodes readByteCode(ClassSources classSources) {
        if (classSources.notFound()) {
            throw new ClassFindFailException();
        }

        return byteCodeFactory.readFrom(classSources);
    }

    /**
     * ソースからSQLを読み取る
     */
    public Sqls readSql(SqlSources sqlSources) {
        return sqlReader.readFrom(sqlSources);
    }
}
