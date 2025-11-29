package org.dddjava.jig;

import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;

import java.nio.file.Path;
import java.util.List;

/**
 * JigDocumentごとの処理結果
 */
public interface HandleResult {
    static HandleResult withException(JigDocument jigDocument, Exception e) {
        return new HandleResultImpl(jigDocument, e);
    }

    static HandleResult withOutput(JigDocument jigDocument, List<Path> outputFilePaths) {
        return new HandleResultImpl(jigDocument, outputFilePaths);
    }

    JigDocument jigDocument();

    /**
     * 出力されたものがダイアグラムか否か
     */
    boolean isOutputDiagram();

    /**
     * 成否
     */
    boolean success();

    /**
     * 出力されたファイルの絶対パスリストを結合した文字列
     */
    String outputFilePathsText();

    /**
     * 出力されたファイル名のリスト
     */
    List<String> outputFileNames();
}
