package org.dddjava.jig.infrastructure.view.poi.report;

import java.util.List;

/**
 * 一覧のヘッダ
 */
public class Header {

    List<ReportItemMethod> reportItemMethods;

    public Header(List<ReportItemMethod> reportItemMethods) {
        this.reportItemMethods = reportItemMethods;
    }

    public String textOf(int i) {
        return reportItemMethods.get(i).label();
    }

    public int size() {
        return reportItemMethods.size();
    }
}
