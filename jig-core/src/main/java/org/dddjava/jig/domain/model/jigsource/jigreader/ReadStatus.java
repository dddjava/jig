package org.dddjava.jig.domain.model.jigsource.jigreader;

import org.dddjava.jig.domain.model.parts.rdbaccess.SqlReadStatus;

/**
 * 読み取り結果
 */
public enum ReadStatus {
    テキストソースなし("implementation.TextSourceNotFound"),
    バイナリソースなし("implementation.BinarySourceNotFound"),
    SQLなし("implementation.SqlNotFound"),
    SQL読み込み一部失敗("implementation.SqlReadWarning"),
    SQL読み込み失敗("implementation.SqlReadError");

    public String messageKey;

    ReadStatus(String messageKey) {
        this.messageKey = messageKey;
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
