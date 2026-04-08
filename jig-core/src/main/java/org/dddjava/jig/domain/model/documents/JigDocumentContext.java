package org.dddjava.jig.domain.model.documents;

import java.nio.file.Path;
import java.util.List;

/**
 * ドキュメント出力時のコンテキスト
 *
 * ドキュメント出力の主体となるインスタンスが持っていない情報（特に用語）を提供する役割
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
