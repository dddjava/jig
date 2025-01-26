package org.dddjava.jig.application;

import org.dddjava.jig.annotation.Service;
import org.dddjava.jig.domain.model.data.JigDataProvider;
import org.dddjava.jig.domain.model.data.classes.rdbaccess.MyBatisStatements;
import org.dddjava.jig.domain.model.data.classes.type.ClassComment;
import org.dddjava.jig.domain.model.data.packages.PackageComment;
import org.dddjava.jig.domain.model.sources.*;
import org.dddjava.jig.domain.model.sources.classsources.ClassSourceModel;
import org.dddjava.jig.domain.model.sources.classsources.ClassSourceReader;
import org.dddjava.jig.domain.model.sources.classsources.ClassSources;
import org.dddjava.jig.domain.model.sources.javasources.JavaSourceModel;
import org.dddjava.jig.domain.model.sources.javasources.JavaSourceReader;
import org.dddjava.jig.domain.model.sources.javasources.TextSources;
import org.dddjava.jig.domain.model.sources.mybatis.SqlReader;
import org.dddjava.jig.domain.model.sources.mybatis.SqlSources;
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

    final ClassSourceReader classSourceReader;
    final JavaSourceReader javaSourceReader;
    final SqlReader sqlReader;

    public JigSourceReader(CommentRepository commentRepository, ClassSourceReader classSourceReader, JavaSourceReader javaSourceReader, SqlReader sqlReader, SourceReader sourceReader) {
        this.commentRepository = commentRepository;
        this.classSourceReader = classSourceReader;
        this.javaSourceReader = javaSourceReader;
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

        JavaSourceModel javaSourceModel = javaSourceReader.javaSourceModel(textSources);
        for (ClassComment classComment : javaSourceModel.classCommentList()) {
            commentRepository.register(classComment);
        }
        for (PackageComment packageComment : javaSourceModel.packageComments()) {
            commentRepository.register(packageComment);
        }

        ClassSources classSources = sources.classSources();
        ClassSourceModel classSourceModel = classSourceReader.classSourceModel(classSources);

        return new DefaultJigDataProvider(classSourceModel, javaSourceModel);
    }

    /**
     * ソースからSQLを読み取る
     */
    public MyBatisStatements readSqlSource(SqlSources sqlSources) {
        return sqlReader.readFrom(sqlSources);
    }
}
