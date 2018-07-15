package org.dddjava.jig.domain.model.controllers;

import org.dddjava.jig.domain.model.declaration.method.Method;
import org.dddjava.jig.domain.model.report.ReportItem;
import org.dddjava.jig.domain.model.report.ReportItemFor;

/**
 * コントローラーの切り口
 */
public class ControllerAngle {

    private final Method method;

    public ControllerAngle(Method method) {
        this.method = method;
    }

    @ReportItemFor(ReportItem.クラス名)
    @ReportItemFor(ReportItem.メソッド名)
    @ReportItemFor(ReportItem.メソッド戻り値の型)
    public Method method() {
        return method;
    }
}
