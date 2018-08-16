package org.dddjava.jig.domain.basic;

import org.springframework.core.env.Environment;

import java.text.MessageFormat;
import java.util.StringJoiner;

/**
 * ユーザーへの警告
 */
public enum Warning {
    クラス検出異常(new Message()
            .add("解析対象の *.class ファイルが見つからないため、処理を中断しました。")
            .add("ビルドされていないか、出力先ディレクトリの指定が誤っています。")
            .add("")
            .add("{0}")) {
        @Override
        public String textWithSpringEnvironment(Environment environment) {
            String propertyValue = getProperty(environment, "directory.classes");
            String variable = new StringJoiner(System.lineSeparator())
                    .add("以下の値を確認してください。この値はディレクトリの絞り込みに使用されます。")
                    .add("- directory.classes: " + propertyValue).toString();
            return text(variable);
        }

        @Override
        public String with(ConfigurationContext context) {
            return text(context.classFileDetectionWarningMessage());
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
            .add("{0}")) {
        @Override
        public String textWithSpringEnvironment(Environment environment) {
            String propertyValue = getProperty(environment, "jig.model.pattern");
            String variable = new StringJoiner(System.lineSeparator())
                    .add("以下の値を確認してください。")
                    .add("- - jig.model.pattern: " + propertyValue).toString();
            return text(variable);
        }

        @Override
        public String with(ConfigurationContext context) {
            return text(context.modelDetectionWarningMessage());
        }
    },
    Mapperメソッド検出異常(new Message()
            .add("Mapperメソッドが見つからないため、データソース一覧のCRUDが空になります。")
            .add("CRUDの検出は以下の条件を満たす必要があります。")
            .add("")
            .add("- MyBatisのMapperインタフェースを使用している")
            .add("- Mapperインタフェース名が *Mapper である")),
    ;

    String messageFormatPattern;

    Warning(Message messageFormatPattern) {
        this.messageFormatPattern = messageFormatPattern.toString();
    }

    // TODO spring依存させないいい感じの方法
    // メッセージに出力するキー名と取得するキー名を記述する箇所を散らばらせたくない。
    public String textWithSpringEnvironment(Environment environment) {
        throw new UnsupportedOperationException();
    }

    public String with(ConfigurationContext configurationContext) {
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
