package org.dddjava.jig.application;

import org.dddjava.jig.annotation.Repository;
import org.dddjava.jig.domain.model.documents.Diagnostic;
import org.dddjava.jig.domain.model.documents.JigIssue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.*;

/**
 * 生成における情報を記録する
 */
@Repository
public class JigEventRepository {
    private static final Logger logger = LoggerFactory.getLogger(JigEventRepository.class);

    private final Collection<JigIssue> jigIssues = EnumSet.noneOf(JigIssue.class);
    private final Locale locale;

    public JigEventRepository(Locale locale) {
        this.locale = locale;
    }

    public void recordIssue(JigIssue jigIssue) {
        jigIssues.add(jigIssue);
    }

    public void notifyWithLogger() {
        jigIssues.forEach(jigIssue -> {
            var message = jigIssue.localizedMessage(locale);
            if (jigIssue.isError()) {
                logger.error(message);
            } else {
                logger.warn(message);
            }
        });
    }

    public boolean hasError() {
        return jigIssues.stream().anyMatch(JigIssue::isError);
    }

    /**
     * 記録した事象を、生成物に埋め込める形で返す。
     */
    public List<Diagnostic> diagnostics() {
        return jigIssues.stream()
                .map(JigIssue::toDiagnostic)
                .sorted(Comparator.comparing(Diagnostic::code))
                .toList();
    }

    public void register指定されたパスが存在しない(Path basePath) {
        logger.info("'{}' が指定されましたが、存在しません。読み飛ばします。", basePath);
    }

    public void registerパスの収集に失敗しました(Path basePath, Exception e) {
        logger.warn("パス {} 配下のファイルの収集に失敗しました。スキップして続行しますが、このパス配下の情報は結果に含まれません。" +
                        "読み取れないパスが指定された場合などに発生します。実行環境かパスの指定を見直してください。(type={}, message={})",
                basePath, e.getClass().getName(), e.getMessage(), e);
    }
}
