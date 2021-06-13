package org.dddjava.jig.domain.model.sources.jigreader;

import org.dddjava.jig.domain.model.parts.rdbaccess.SqlReadStatus;

import java.util.Locale;

/**
 * 読み取り結果
 */
public enum ReadStatus {
    テキストソースなし(
            "ソースファイル(*.java)が見つかりませんでした。ソースディレクトリの指定を確認してください。このメッセージが出る場合、別名が出力できません。",
            "Source file(*.java) was not found. Check the specification of the source directory. If this message appears, Japanese name can not be output."),
    バイナリソースなし(
            "コンパイルされたファイル(*.class)が見つかりませんでした。出力ディレクトリの指定を確認してください。",
            "Compiled file(*.class) was not found. Check the output directory specification."),
    SQLなし(
            "SQLが見つかりませんでした。SQLを実装していない場合やMyBatisを使用していない場合は正常です。このメッセージが出る場合、データソース一覧にCRUDが出力されません。",
            "SQL was not found. It is normal if you do not implement SQL or if you are not using MyBatis. If this message appears, CRUD is not output in the data source list."),
    SQL読み込み一部失敗(
            "SQLの読み込みに一部失敗しました。CRUDの出力に欠落が存在します。",
            "Partial loading of SQL failed. There is a missing in the output of CRUD."),
    SQL読み込み失敗(
            "SQLの読み込みに失敗しました。CRUDは出力されません。",
            "SQL reading failed. CRUD is not output.");

    public String message;

    ReadStatus(String... messages) {
        Locale locale = Locale.getDefault();
        boolean isEnglish = locale.getLanguage().equals("en");
        this.message = isEnglish ? messages[1] : messages[0];
    }

    public String localizedMessage() {
        return message;
    }

    public static ReadStatus fromSqlReadStatus(SqlReadStatus sqlReadStatus) {
        switch (sqlReadStatus) {
            case SQLなし:
                return SQLなし;
            case 読み取り失敗あり:
                return SQL読み込み一部失敗;
            case 失敗:
                return SQL読み込み失敗;
        }
        throw new IllegalArgumentException(sqlReadStatus.toString());
    }

    public boolean isError() {
        return this == バイナリソースなし;
    }
}
