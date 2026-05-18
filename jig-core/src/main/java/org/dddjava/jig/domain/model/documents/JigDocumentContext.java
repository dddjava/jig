package org.dddjava.jig.domain.model.documents;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

/**
 * ドキュメント出力時のコンテキスト
 */
public interface JigDocumentContext {

    /**
     * ドキュメント出力先ディレクトリ
     */
    Path outputDirectory();

    /**
     * 処理対象となるJigDocument
     */
    List<JigDocument> jigDocuments();

    /**
     * 出力時の表示言語。実行環境ではなく JIG 設定で決まる。
     */
    Locale locale();

}
