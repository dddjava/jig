package org.dddjava.jig.domain.model.documents.stationery;

import java.util.Locale;

/**
 * ユーザーへの警告
 */
public enum Warning {
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

    String message;

    Warning(String... message) {
        Locale locale = Locale.getDefault();
        boolean isEnglish = locale.getLanguage().equals("en");
        this.message = isEnglish ? message[1] : message[0];
    }

    public String localizedMessage() {
        return message;
    }
}
