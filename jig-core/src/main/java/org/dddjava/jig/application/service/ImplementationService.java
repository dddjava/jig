package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.rdbaccess.Sqls;
import org.dddjava.jig.domain.model.jigsource.file.SourcePaths;
import org.dddjava.jig.domain.model.jigsource.file.SourceReader;
import org.dddjava.jig.domain.model.jigsource.file.Sources;
import org.dddjava.jig.domain.model.jigsource.file.binary.ClassSources;
import org.dddjava.jig.domain.model.jigsource.file.text.sqlcode.SqlSources;
import org.dddjava.jig.domain.model.jigsource.jigloader.SqlReader;
import org.dddjava.jig.domain.model.jigsource.jigloader.analyzed.AnalyzedImplementation;
import org.dddjava.jig.domain.model.jigsource.jigloader.analyzed.FactFactory;
import org.dddjava.jig.domain.model.jigsource.jigloader.analyzed.TypeFacts;
import org.springframework.stereotype.Service;

/**
 * 取り込みサービス
 */
@Service
public class ImplementationService {

    AliasService aliasService;

    FactFactory factFactory;
    SqlReader sqlReader;

    SourceReader sourceReader;

    public ImplementationService(FactFactory factFactory, AliasService aliasService, SqlReader sqlReader, SourceReader sourceReader) {
        this.factFactory = factFactory;
        this.aliasService = aliasService;
        this.sqlReader = sqlReader;
        this.sourceReader = sourceReader;
    }

    public AnalyzedImplementation implementations(SourcePaths sourcePaths) {
        Sources source = sourceReader.readSources(sourcePaths);

        TypeFacts typeFacts = readProjectData(source);
        Sqls sqls = readSql(source.sqlSources());

        return AnalyzedImplementation.generate(source, typeFacts, sqls);
    }

    /**
     * プロジェクト情報を読み取る
     */
    public TypeFacts readProjectData(Sources sources) {
        TypeFacts typeFacts = readByteCode(sources.classSources());

        aliasService.loadAliases(sources.aliasSource());

        return typeFacts;
    }

    /**
     * ソースからバイトコードを読み取る
     */
    public TypeFacts readByteCode(ClassSources classSources) {
        return factFactory.readTypeFacts(classSources);
    }

    /**
     * ソースからSQLを読み取る
     */
    public Sqls readSql(SqlSources sqlSources) {
        return sqlReader.readFrom(sqlSources);
    }
}
