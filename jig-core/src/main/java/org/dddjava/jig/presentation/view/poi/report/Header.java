package org.dddjava.jig.presentation.view.poi.report;

import java.util.List;

/**
 * 一覧のヘッダ
 */
public class Header {

    List<ReportItemMethod> reportItemMethods;

    public Header(List<ReportItemMethod> reportItemMethods) {
        this.reportItemMethods = reportItemMethods;
    }

    public String textOf(int i, ReportItemFormatter reportItemFormatter) {
        return reportItemMethods.get(i).label(reportItemFormatter);
    }

    public int size() {
        return reportItemMethods.size();
    }
}
