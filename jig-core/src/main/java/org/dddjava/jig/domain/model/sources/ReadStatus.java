package org.dddjava.jig.domain.model.sources;

import org.dddjava.jig.domain.model.documents.JigDocument;
import org.dddjava.jig.domain.model.documents.LocalizedMessage;

import java.util.List;
import java.util.Locale;

/**
 * 読み取り結果
 *
 * jigDocuments はこの結果で内容が欠けるドキュメント。解析そのものが成立しないものは全体に影響するため空にする。
 */
public enum ReadStatus {
    テキストソースなし(List.of(JigDocument.Glossary), new LocalizedMessage(
            "テキストソース(*.javaなど)が見つかりませんでした。ソースディレクトリの指定を確認してください。このメッセージが出る場合、テキストソース由来の情報が出力できません。",
            "Text Source file(*.java, etc) was not found. Check the specification of the source directory. If this message appears, alias can not be output.")),
    バイナリソースなし(List.of(), new LocalizedMessage(
            "バイナリソース(*.class)が見つかりませんでした。出力ディレクトリの指定を確認してください。",
            "Binary Source file(*.class) was not found. Check the output directory specification.")),
    テキストソース読み込み一部失敗(List.of(JigDocument.Glossary), new LocalizedMessage(
            "テキストソースの読み込みに一部失敗しました。Javadocなどテキストソース由来の情報に欠落が存在します。",
            "Partial loading of text sources failed. Information derived from text sources, such as Javadoc, may be missing.")),
    SQLなし(List.of(JigDocument.OutboundInterface), new LocalizedMessage(
            "SQLが見つかりませんでした。SQLを実装していない場合やMyBatis・Spring Data JDBCを使用していない場合は正常です。CRUDに関わる情報が出力されません。",
            "SQL was not found. It is normal if you do not implement SQL or if you are not using MyBatis/Spring Data JDBC. If this message appears, CRUD is not output in the data source list.")),
    SQL読み込み一部失敗(List.of(JigDocument.OutboundInterface), new LocalizedMessage(
            "SQLの読み込みに一部失敗しました。CRUDの出力に欠落が存在します。",
            "Partial loading of SQL failed. There is a missing in the output of CRUD.")),
    SQL読み込み失敗(List.of(JigDocument.OutboundInterface), new LocalizedMessage(
            "SQLの読み込みに失敗しました。CRUDは出力されません。",
            "SQL reading failed. CRUD is not output."));

    private final List<JigDocument> jigDocuments;
    private final LocalizedMessage message;

    ReadStatus(List<JigDocument> jigDocuments, LocalizedMessage message) {
        this.jigDocuments = jigDocuments;
        this.message = message;
    }

    public List<JigDocument> jigDocuments() {
        return jigDocuments;
    }

    public LocalizedMessage message() {
        return message;
    }

    public String localizedMessage(Locale locale) {
        return message.forLocale(locale);
    }

    public boolean isError() {
        return this == バイナリソースなし;
    }
}
