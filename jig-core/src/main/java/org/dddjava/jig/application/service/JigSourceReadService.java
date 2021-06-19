package org.dddjava.jig.application.service;

import org.dddjava.jig.application.repository.JigSourceRepository;
import org.dddjava.jig.domain.model.parts.classes.method.MethodComment;
import org.dddjava.jig.domain.model.parts.classes.type.ClassComment;
import org.dddjava.jig.domain.model.parts.packages.PackageComment;
import org.dddjava.jig.domain.model.parts.packages.PackageComments;
import org.dddjava.jig.domain.model.parts.rdbaccess.Sqls;
import org.dddjava.jig.domain.model.sources.file.SourcePaths;
import org.dddjava.jig.domain.model.sources.file.SourceReader;
import org.dddjava.jig.domain.model.sources.file.Sources;
import org.dddjava.jig.domain.model.sources.file.binary.ClassSources;
import org.dddjava.jig.domain.model.sources.file.text.CodeSources;
import org.dddjava.jig.domain.model.sources.file.text.sqlcode.SqlSources;
import org.dddjava.jig.domain.model.sources.jigfactory.TypeFacts;
import org.dddjava.jig.domain.model.sources.jigreader.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 取り込みサービス
 */
@Service
public class JigSourceReadService {

    final JigSourceRepository jigSourceRepository;

    final SourceReader sourceReader;

    final FactReader binarySourceReader;
    final TextSourceReader textSourceReader;
    final SqlReader sqlReader;

    public JigSourceReadService(JigSourceRepository jigSourceRepository, FactReader binarySourceReader, TextSourceReader textSourceReader, SqlReader sqlReader, SourceReader sourceReader) {
        this.jigSourceRepository = jigSourceRepository;
        this.binarySourceReader = binarySourceReader;
        this.textSourceReader = textSourceReader;
        this.sqlReader = sqlReader;
        this.sourceReader = sourceReader;
    }

    /**
     * パスからソースを読み取る
     */
    public ReadStatuses readSourceFromPaths(SourcePaths sourcePaths) {
        Sources source = sourceReader.readSources(sourcePaths);

        readProjectData(source);
        Sqls sqls = readSqlSource(source.sqlSources());

        List<ReadStatus> list = new ArrayList<>();

        if (source.nothingBinarySource()) {
            list.add(ReadStatus.バイナリソースなし);
        }

        if (source.nothingTextSource()) {
            list.add(ReadStatus.テキストソースなし);
        }

        // binarySourceがあってtypeByteCodesがない（ASMの解析で失敗する）のは現状実行時エラーになるのでここでは考慮しない

        if (sqls.status().not正常()) {
            list.add(ReadStatus.fromSqlReadStatus(sqls.status()));
        }

        return new ReadStatuses(list);
    }

    /**
     * プロジェクト情報を読み取る
     */
    public TypeFacts readProjectData(Sources sources) {
        TypeFacts typeFacts = readBinarySources(sources.classSources());
        readTextSources(sources.textSources());
        return typeFacts;
    }

    /**
     * ソースからバイトコードを読み取る
     */
    TypeFacts readBinarySources(ClassSources classSources) {
        TypeFacts typeFacts = binarySourceReader.readTypeFacts(classSources);
        jigSourceRepository.registerTypeFact(typeFacts);
        return typeFacts;
    }

    /**
     * コメントを読み取る
     */
    void readTextSources(CodeSources codeSources) {
        ClassAndMethodComments classAndMethodComments = textSourceReader.readClassAndMethodComments(codeSources);
        for (ClassComment classComment : classAndMethodComments.list()) {
            jigSourceRepository.registerClassComment(classComment);
        }
        for (MethodComment methodComment : classAndMethodComments.methodList()) {
            jigSourceRepository.registerMethodComment(methodComment);
        }

        PackageComments packageComments = textSourceReader.readPackageComments(codeSources);
        for (PackageComment packageComment : packageComments.list()) {
            jigSourceRepository.registerPackageComment(packageComment);
        }
    }

    /**
     * ソースからSQLを読み取る
     */
    public Sqls readSqlSource(SqlSources sqlSources) {
        Sqls sqls = sqlReader.readFrom(sqlSources);
        jigSourceRepository.registerSqls(sqls);
        return sqls;
    }
}
