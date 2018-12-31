package org.dddjava.jig.domain.type;

import java.util.ResourceBundle;

/**
 * ユーザーへの警告
 */
public enum Warning {
    クラスが見つからないので中断する通知(),
    ハンドラメソッドが見つからないので出力されない通知(),
    サービスメソッドが見つからないので出力されない通知(),
    ビジネスルールが見つからないので出力されない通知(),
    リポジトリが見つからないので出力されない通知(),
    SQLが見つからないので出力されない通知(),
    ;

    Warning() {
    }

    public String text() {
        ResourceBundle resource = ResourceBundle.getBundle("jig-messages");
        return resource.getString(name());
    }
}
