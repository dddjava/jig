package org.dddjava.jig.application;

import org.dddjava.jig.annotation.Service;
import org.dddjava.jig.domain.model.data.JigDataProvider;
import org.dddjava.jig.domain.model.data.classes.rdbaccess.MyBatisStatements;
import org.dddjava.jig.domain.model.data.classes.type.ClassComment;
import org.dddjava.jig.domain.model.data.packages.PackageComment;
import org.dddjava.jig.domain.model.sources.DefaultJigDataProvider;
import org.dddjava.jig.domain.model.sources.file.SourcePaths;
import org.dddjava.jig.domain.model.sources.file.SourceReader;
import org.dddjava.jig.domain.model.sources.file.Sources;
import org.dddjava.jig.domain.model.sources.file.binary.ClassSources;
import org.dddjava.jig.domain.model.sources.file.text.TextSources;
import org.dddjava.jig.domain.model.sources.file.text.sqlcode.SqlSources;
import org.dddjava.jig.domain.model.sources.jigfactory.ByteSourceModel;
import org.dddjava.jig.domain.model.sources.jigfactory.TextSourceModel;
import org.dddjava.jig.domain.model.sources.jigreader.FactReader;
import org.dddjava.jig.domain.model.sources.jigreader.JavaTextSourceReader;
import org.dddjava.jig.domain.model.sources.jigreader.ReadStatus;
import org.dddjava.jig.domain.model.sources.jigreader.SqlReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    final JavaTextSourceReader javaTextSourceReader;
    final SqlReader sqlReader;

    public JigSourceReader(CommentRepository commentRepository, FactReader binarySourceReader, JavaTextSourceReader javaTextSourceReader, SqlReader sqlReader, SourceReader sourceReader) {
        this.commentRepository = commentRepository;
        this.binarySourceReader = binarySourceReader;
        this.javaTextSourceReader = javaTextSourceReader;
        this.sqlReader = sqlReader;
        this.sourceReader = sourceReader;
    }

    public Optional<JigDataProvider> readPathSource(SourcePaths sourcePaths) {
        List<ReadStatus> readEvents = new ArrayList<>();

        // ソースのチェック
        Sources source = sourceReader.readSources(sourcePaths);
        if (source.nothingBinarySource()) readEvents.add(ReadStatus.バイナリソースなし);
        if (source.nothingTextSource()) readEvents.add(ReadStatus.テキストソースなし);
        // binarySourceがあってtypeByteCodesがない（ASMの解析で失敗する）のは現状実行時エラーになるのでここでは考慮しない

        MyBatisStatements myBatisStatements = readSqlSource(source.sqlSources());
        if (myBatisStatements.status().not正常())
            readEvents.add(ReadStatus.fromSqlReadStatus(myBatisStatements.status()));

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
        jigSource.addSqls(myBatisStatements);
        return Optional.of(jigSource);
    }

    /**
     * プロジェクト情報を読み取る
     */
    public DefaultJigDataProvider readProjectData(Sources sources) {
        TextSources textSources = sources.textSources();

        TextSourceModel textSourceModel = javaTextSourceReader.textSourceModel(textSources);
        for (ClassComment classComment : textSourceModel.classCommentList()) {
            commentRepository.register(classComment);
        }
        for (PackageComment packageComment : textSourceModel.packageComments()) {
            commentRepository.register(packageComment);
        }

        ClassSources classSources = sources.classSources();
        ByteSourceModel byteSourceModel = binarySourceReader.byteSourceModel(classSources);

        return new DefaultJigDataProvider(byteSourceModel, textSourceModel);
    }

    /**
     * ソースからSQLを読み取る
     */
    public MyBatisStatements readSqlSource(SqlSources sqlSources) {
        return sqlReader.readFrom(sqlSources);
    }
}
