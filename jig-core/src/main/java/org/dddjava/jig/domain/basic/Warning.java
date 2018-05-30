package org.dddjava.jig.domain.basic;

import org.springframework.core.env.Environment;

import java.text.MessageFormat;
import java.util.StringJoiner;

public enum Warning {
    クラス検出異常(new Message()
            .add("解析対象の *.class ファイルが見つからないため、処理を中断しました。")
            .add("ビルドされていないか、出力先ディレクトリの指定が誤っています。")
            .add("")
            .add("以下の値を確認してください。この値はディレクトリの絞り込みに使用されます。")
            .add("- build.classes: {0}")) {
        @Override
        public String textWithSpringEnvironment(Environment environment) {
            return text(getProperty(environment, "build.classes"));
        }
    },
    サービス検出異常(new Message()
            .add("サービスクラスが見つからないため、サービス関連図やサービス一覧が空になります。")
            .add("検出には org.springframework.stereotype.Service アノテーションを使用しています。")
            .add("対象のサービスクラスにアノテーションを付与してください。")),
    モデル検出異常(new Message()
            .add("モデルが検出できないため、パッケージ関連図が空になります。")
            .add("パッケージ構成が想定と異なる可能性があります。")
            .add("")
            .add("以下の値を確認してください。")
            .add("- jig.model.pattern: {0}")) {
        @Override
        public String textWithSpringEnvironment(Environment environment) {
            return text(getProperty(environment, "jig.model.pattern"));
        }
    };

    String messageFormatPattern;

    Warning(Message messageFormatPattern) {
        this.messageFormatPattern = messageFormatPattern.toString();
    }

    // TODO spring依存させないいい感じの方法
    // メッセージに出力するキー名と取得するキー名を記述する箇所を散らばらせたくない。
    public String textWithSpringEnvironment(Environment environment) {
        throw new UnsupportedOperationException();
    }

    public String getProperty(Environment environment, String key) {
        if (environment == null) return "（取得できません）";
        return environment.getProperty(key);
    }

    public String text(Object... args) {
        return MessageFormat.format(messageFormatPattern, args);
    }

    private static class Message {
        StringJoiner joiner = new StringJoiner(System.lineSeparator())
                .add("")
                .add("================================================================================");

        Message add(String newLine) {
            joiner.add(newLine);
            return this;
        }

        @Override
        public String toString() {
            joiner.add("");
            joiner.add("より詳しい情報は https://github.com/dddjava/Jig/wiki/HELP を参照してください。");
            joiner.add("================================================================================");
            return joiner.toString();
        }
    }
}
