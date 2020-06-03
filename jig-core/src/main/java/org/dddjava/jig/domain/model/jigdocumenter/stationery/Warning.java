package org.dddjava.jig.domain.model.jigdocumenter.stationery;

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

    public String resourceKey() {
        return key;
    }
}
