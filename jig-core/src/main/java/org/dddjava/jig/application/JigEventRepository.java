package org.dddjava.jig.application;

import org.dddjava.jig.annotation.Repository;
import org.dddjava.jig.domain.model.sources.ReadStatus;
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

    private final Collection<ReadStatus> readStatuses = EnumSet.noneOf(ReadStatus.class);
    private final Set<Warning> warnings = new HashSet<>();

    public void registerコアドメインが見つからない() {
        warnings.add(Warning.ビジネスルールが見つからないので出力されない通知);
    }

    public void registerエントリーポイントが見つからない() {
        warnings.add(Warning.ハンドラメソッドが見つからないので出力されない通知);
    }

    public void registerリポジトリが見つからない() {
        warnings.add(Warning.リポジトリが見つからないので出力されない通知);
    }

    public void registerサービスが見つからない() {
        warnings.add(Warning.サービスメソッドが見つからないので出力されない通知);
    }

    public void notifyWithLogger() {
        warnings.stream().map(Warning::localizedMessage).forEach(logger::warn);
    }

    public void recordEvent(ReadStatus readStatus) {
        readStatuses.add(readStatus);
        var message = readStatus.localizedMessage();
        if (readStatus.isError()) {
            logger.error(message);
        } else {
            logger.warn(message);
        }
    }

    public boolean hasError() {
        return readStatuses.stream().anyMatch(ReadStatus::isError);
    }

    public void register指定されたパスが存在しない(Path basePath) {
        logger.info("'{}' が指定されましたが、存在しません。読み飛ばします。", basePath);
    }

    public void registerパスの収集に失敗しました(Path basePath, Exception e) {
        logger.warn("パス {} 配下のファイルの収集に失敗しました。スキップして続行しますが、このパス配下の情報は結果に含まれません。" +
                        "読み取れないパスが指定された場合などに発生します。実行環境かパスの指定を見直してください。(type={}, message={})",
                basePath, e.getClass().getName(), e.getMessage(), e);
    }

    /**
     * ユーザーへの警告
     */
    private enum Warning {
        ハンドラメソッドが見つからないので出力されない通知(
                "リクエストハンドラメソッドが見つからないため、コントローラーに関わる情報は出力されません。@Controllerや@RestControllerがない場合は正常です。",
                "Request handler method cannot be found. Request handler method requires class annotated by @Controller or @RestController, and method annotated by @RequestMapping."),
        サービスメソッドが見つからないので出力されない通知(
                "サービスメソッドが見つからないため、サービスに関わる情報は出力されません。@Serviceがない場合は正常です。",
                "Service method cannot be found. Service method requires class annotated by @Service."),
        ビジネスルールが見つからないので出力されない通知(
                "ビジネスルールが識別できないため、パッケージ関連図を出力できません。パッケージ構成を確認してください。",
                "Business Rule cannot be found. Please check the package layout."),
        リポジトリが見つからないので出力されない通知(
                "Repositoryのメソッドが見つからないため、データソースに関わる情報は出力されません。@Repositoryがない場合は正常です。",
                "Repository method cannot be found."),
        ;

        private final String message;

        Warning(String japaneseMessage, String englishMessage) {
            Locale locale = Locale.getDefault();
            boolean isEnglish = locale.getLanguage().equals("en");
            this.message = isEnglish ? englishMessage : japaneseMessage;
        }

        public String localizedMessage() {
            return message;
        }
    }
}
