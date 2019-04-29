package org.dddjava.jig.presentation.view.poi.report;

import java.util.List;

/**
 * 一覧のヘッダ
 */
public class Header {

    List<String> list;

    Header(List<String> list) {
        this.list = list;
    }

    public String textOf(int i) {
        return list.get(i);
    }

    public int size() {
        return list.size();
    }
}
