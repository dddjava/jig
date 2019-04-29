package org.dddjava.jig.presentation.view.poi.report.formatter;

import org.dddjava.jig.presentation.view.report.ReportItem;

/**
 * 一覧出力項目のフォーマッターインターフェース
 */
interface ReportItemFormatter {

    boolean canFormat(Object item);

    String format(ReportItem itemCategory, Object item);
}
