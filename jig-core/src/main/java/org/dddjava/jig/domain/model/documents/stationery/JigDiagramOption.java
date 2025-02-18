package org.dddjava.jig.domain.model.documents.stationery;

import org.dddjava.jig.domain.model.documents.documentformat.JigDiagramFormat;

/**
 * ダイアグラム出力オプション
 * @param graphvizOutputFormat graphvizの出力フォーマット
 * @param transitiveReduction 推移簡約をするかどうか
 */
public record JigDiagramOption(
        JigDiagramFormat graphvizOutputFormat,
        boolean transitiveReduction
) {
}
