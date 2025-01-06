package org.dddjava.jig.infrastructure.view.poi.report;

import org.apache.poi.ss.usermodel.Workbook;
import org.dddjava.jig.application.JigDocumentWriter;

public interface ModelReportInterface {

    void writeSheet(Workbook book, JigDocumentWriter jigDocumentWriter, ReportItemFormatter reportItemFormatter);

    boolean nothing();
}
