package org.dddjava.jig.domain.model.documents.stationery;

import org.dddjava.jig.domain.model.documents.documentformat.JigDiagramFormat;

import java.time.Duration;

/**
 * ダイアグラム出力オプション
 * @param graphvizOutputFormat graphvizの出力フォーマット
 * @param graphvizTimeout dotコマンドのタイムアウト
 * @param transitiveReduction 推移簡約をするかどうか
 */
public record JigDiagramOption(
        JigDiagramFormat graphvizOutputFormat,
        Duration graphvizTimeout,
        boolean transitiveReduction
) {
}
