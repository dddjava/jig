package org.dddjava.jig.application;

import org.dddjava.jig.annotation.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * 生成における情報を記録する
 */
@Repository
public class JigReporter {
    private static final Logger logger = LoggerFactory.getLogger(JigReporter.class);

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
            this.message = isEnglish ? japaneseMessage : englishMessage;
        }

        public String localizedMessage() {
            return message;
        }
    }
}
