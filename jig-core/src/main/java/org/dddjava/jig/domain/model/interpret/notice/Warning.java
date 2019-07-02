package org.dddjava.jig.domain.model.interpret.notice;

import org.dddjava.jig.infrastructure.resourcebundle.Utf8ResourceBundle;

import java.util.ResourceBundle;

/**
 * ユーザーへの警告
 */
public enum Warning {
    ハンドラメソッドが見つからないので出力されない通知("warning.RequestHandlerNotFound"),
    サービスメソッドが見つからないので出力されない通知("warning.ServiceMethodNotFound"),
    ビジネスルールが見つからないので出力されない通知("warning.BusinessRuleNotFound"),
    リポジトリが見つからないので出力されない通知("warning.RepositoryNotFound"),
    ;

    String key;

    Warning(String key) {
        this.key = key;
    }

    public String text() {
        ResourceBundle resource = Utf8ResourceBundle.messageBundle();
        return resource.getString(key);
    }
}
