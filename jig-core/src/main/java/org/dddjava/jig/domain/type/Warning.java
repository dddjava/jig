package org.dddjava.jig.domain.type;

import org.dddjava.jig.domain.model.configuration.ConfigurationContext;
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
        public String with(ConfigurationContext context) {
            return text(context.classFileDetectionWarningMessage());
        }
    },
    コントローラーなし(new Message()
            .add("リクエストハンドラメソッドが見つからないため、コントローラー一覧が出力されません。")
            .add("リクエストハンドラメソッドの対象は次の通りです。")
            .add("")
            .add("- クラスに @Controller 系のアノテーションが付与されている")
            .add("- メソッドに @RequestMapping 系のアノテーションが付与されている。")
    ),
    サービスなし(new Message()
            .add("サービスメソッドが見つからないため、サービス関連図やサービス一覧が出力されません。")
            .add("必要な条件は次の通りです。")
            .add("")
            .add("- クラスに @Service アノテーションが付与されている")
            .add("- メソッドが1つ以上実装されている")
    ),
    モデル検出異常(new Message()
            .add("モデルが検出できないため、パッケージ関連図が空になります。")
            .add("パッケージ構成が想定と異なる可能性があります。")
            .add("")
            .add("{0}")) {
        @Override
        public String with(ConfigurationContext context) {
            return text(context.modelDetectionWarningMessage());
        }
    },
    リポジトリなし(new Message()
            .add("Repositoryのメソッドが見つからないため、データソース一覧が出力されません。")
            .add("データソース一覧の出力には以下が必要です。")
            .add("")
            .add("- @Repository がつけられた実装クラス（データソースクラス）")
            .add("- データソースクラスがインタフェース（リポジトリインタフェース）を実装している")
            .add("- リポジトリインタフェースに1メソッド以上定義されている")
    ),
    SQLなし(new Message()
            .add("SQLが見つからないため、データソース一覧のCRUDは出力されません。")
            .add("CRUDの出力には以下の条件を満たす必要があります。")
            .add("")
            .add("- MyBatisのMapperインタフェースを使用している")
            .add("- Mapperインタフェース名が *Mapper である")
            .add("- データソース一覧に出力されるメソッドでMapperインタフェースを使用している")
    ),
    ;

    String messageFormatPattern;

    Warning(Message messageFormatPattern) {
        this.messageFormatPattern = messageFormatPattern.toString();
    }

    public String with(ConfigurationContext configurationContext) {
        throw new UnsupportedOperationException();
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
