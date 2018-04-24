package jig.domain.model.report;

import jig.domain.model.angle.DesignSmellAngle;

import java.util.List;
import java.util.stream.Collectors;

public class StringComparingReport implements Report {

    private final DesignSmellAngle designSmellAngle;

    public StringComparingReport(DesignSmellAngle designSmellAngle) {
        this.designSmellAngle = designSmellAngle;
    }

    @Override
    public List<ReportRow> rows() {
        return designSmellAngle.stringComparingMethods().list().stream()
                .map(methodDeclaration -> ReportRow.of(
                        methodDeclaration.declaringType().fullQualifiedName(),
                        methodDeclaration.methodSignature().asSimpleText()))
                .collect(Collectors.toList());
    }

    @Override
    public Title title() {
        return new Title("文字列比較箇所");
    }

    @Override
    public ReportRow headerRow() {
        return ReportRow.of("クラス名", "メソッド名");
    }
}
