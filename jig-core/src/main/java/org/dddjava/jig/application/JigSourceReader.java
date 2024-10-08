package org.dddjava.jig.application;

import org.dddjava.jig.domain.model.parts.classes.rdbaccess.Sqls;
import org.dddjava.jig.domain.model.parts.classes.type.ClassComment;
import org.dddjava.jig.domain.model.parts.packages.PackageComment;
import org.dddjava.jig.domain.model.sources.file.SourcePaths;
import org.dddjava.jig.domain.model.sources.file.SourceReader;
import org.dddjava.jig.domain.model.sources.file.Sources;
import org.dddjava.jig.domain.model.sources.file.binary.ClassSources;
import org.dddjava.jig.domain.model.sources.file.text.TextSources;
import org.dddjava.jig.domain.model.sources.file.text.sqlcode.SqlSources;
import org.dddjava.jig.domain.model.sources.jigfactory.TextSourceModel;
import org.dddjava.jig.domain.model.sources.jigfactory.TypeFacts;
import org.dddjava.jig.domain.model.sources.jigreader.FactReader;
import org.dddjava.jig.domain.model.sources.jigreader.ReadStatus;
import org.dddjava.jig.domain.model.sources.jigreader.SqlReader;
import org.dddjava.jig.domain.model.sources.jigreader.TextSourceReader;
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

    final CommentRepository commentRepository;

    final SourceReader sourceReader;

    final FactReader binarySourceReader;
    final TextSourceReader textSourceReader;
    final SqlReader sqlReader;

    public JigSourceReader(CommentRepository commentRepository, FactReader binarySourceReader, TextSourceReader textSourceReader, SqlReader sqlReader, SourceReader sourceReader) {
        this.commentRepository = commentRepository;
        this.binarySourceReader = binarySourceReader;
        this.textSourceReader = textSourceReader;
        this.sqlReader = sqlReader;
        this.sourceReader = sourceReader;
    }

    public Optional<JigSource> readPathSource(SourcePaths sourcePaths) {
        List<ReadStatus> readEvents = new ArrayList<>();

        // ソースのチェック
        Sources source = sourceReader.readSources(sourcePaths);
        if (source.nothingBinarySource()) readEvents.add(ReadStatus.バイナリソースなし);
        if (source.nothingTextSource()) readEvents.add(ReadStatus.テキストソースなし);
        // binarySourceがあってtypeByteCodesがない（ASMの解析で失敗する）のは現状実行時エラーになるのでここでは考慮しない

        Sqls sqls = readSqlSource(source.sqlSources());
        if (sqls.status().not正常()) readEvents.add(ReadStatus.fromSqlReadStatus(sqls.status()));

        readEvents.forEach(readStatus -> {
            if (readStatus.isError()) {
                logger.error("{}", readStatus.localizedMessage());
            } else {
                logger.warn("{}", readStatus.localizedMessage());
            }
        });
        // errorが1つでもあったら読み取り失敗としてSourceを返さない
        if (readEvents.stream().anyMatch(event -> event.isError())) {
            return Optional.empty();
        }

        var jigSource = readProjectData(source);
        jigSource.addSqls(sqls);
        return Optional.of(jigSource);
    }

    /**
     * プロジェクト情報を読み取る
     */
    public JigSource readProjectData(Sources sources) {
        ClassSources classSources = sources.classSources();
        TypeFacts typeFacts = binarySourceReader.readTypeFacts(classSources);

        TextSources textSources = sources.textSources();

        TextSourceModel textSourceModel = textSourceReader.readTextSource(textSources);
        for (ClassComment classComment : textSourceModel.classCommentList()) {
            commentRepository.register(classComment);
        }
        for (PackageComment packageComment : textSourceModel.packageComments()) {
            commentRepository.register(packageComment);
        }

        // typeFactsにテキストソースの情報を適用する
        typeFacts.applyTextSource(textSourceModel);

        return new JigSource(typeFacts, textSourceModel.toTerms());
    }

    /**
     * ソースからSQLを読み取る
     */
    public Sqls readSqlSource(SqlSources sqlSources) {
        return sqlReader.readFrom(sqlSources);
    }
}
