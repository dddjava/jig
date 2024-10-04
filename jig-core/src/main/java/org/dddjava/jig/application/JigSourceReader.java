package org.dddjava.jig.application;

import org.dddjava.jig.JigExecutor;
import org.dddjava.jig.domain.model.parts.classes.rdbaccess.Sqls;
import org.dddjava.jig.domain.model.parts.packages.PackageComment;
import org.dddjava.jig.domain.model.parts.packages.PackageComments;
import org.dddjava.jig.domain.model.sources.file.SourcePaths;
import org.dddjava.jig.domain.model.sources.file.SourceReader;
import org.dddjava.jig.domain.model.sources.file.Sources;
import org.dddjava.jig.domain.model.sources.file.binary.ClassSources;
import org.dddjava.jig.domain.model.sources.file.text.TextSources;
import org.dddjava.jig.domain.model.sources.file.text.sqlcode.SqlSources;
import org.dddjava.jig.domain.model.sources.jigfactory.TextSourceModel;
import org.dddjava.jig.domain.model.sources.jigfactory.TypeFacts;
import org.dddjava.jig.domain.model.sources.jigreader.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 取り込みサービス
 */
@Service
public class JigSourceReader {
    private static final Logger logger = LoggerFactory.getLogger(JigSourceReader.class);

    final JigSourceRepository jigSourceRepository;

    final SourceReader sourceReader;

    final FactReader binarySourceReader;
    final TextSourceReader textSourceReader;
    final SqlReader sqlReader;

    public JigSourceReader(JigSourceRepository jigSourceRepository, FactReader binarySourceReader, TextSourceReader textSourceReader, SqlReader sqlReader, SourceReader sourceReader) {
        this.jigSourceRepository = jigSourceRepository;
        this.binarySourceReader = binarySourceReader;
        this.textSourceReader = textSourceReader;
        this.sqlReader = sqlReader;
        this.sourceReader = sourceReader;
    }

    public Optional<JigSource> readSource(SourcePaths sourcePaths) {
        ReadStatuses status = readSourceFromPaths(sourcePaths);
        if (status.hasError()) {
            for (ReadStatus readStatus : status.listErrors()) {
                JigExecutor.logger.error("{}", readStatus.localizedMessage());
            }
            return Optional.empty();
        }
        if (status.hasWarning()) {
            for (ReadStatus readStatus : status.listWarning()) {
                JigExecutor.logger.warn("{}", readStatus.localizedMessage());
            }
        }

        var jigSource = new JigSource(jigSourceRepository.allTypeFacts());
        return Optional.of(jigSource);
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
    public JigSource readProjectData(Sources sources) {
        TypeFacts typeFacts = readBinarySources(sources.classSources());
        readTextSources(sources.textSources());
        return new JigSource(typeFacts);
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
     * ソースからテキストコードを読み取る
     */
    void readTextSources(TextSources textSources) {
        TextSourceModel textSourceModel = textSourceReader.readTextSource(textSources);
        jigSourceRepository.registerTextSourceModel(textSourceModel);

        PackageComments packageComments = textSourceReader.readPackageComments(textSources);
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
