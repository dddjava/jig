package org.dddjava.jig.domain.model.documents;

import java.nio.file.Path;
import java.util.List;

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

}
