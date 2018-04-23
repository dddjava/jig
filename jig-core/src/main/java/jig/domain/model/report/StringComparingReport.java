package jig.domain.model.report;

import jig.domain.model.angle.StringComparing;
import jig.domain.model.declaration.method.MethodDeclarations;
import jig.domain.model.relation.RelationRepository;
import jig.domain.model.report.template.Report;
import jig.domain.model.report.template.ReportRow;
import jig.domain.model.report.template.Title;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class StringComparingReport {
    final StringComparing stringComparing;

    public StringComparingReport(RelationRepository relationRepository) {
        this.stringComparing = new StringComparing(relationRepository);
    }

    public List<ReportRow> rows() {
        MethodDeclarations methodDeclarations = stringComparing.stringComparingMethods();

        return methodDeclarations.list().stream()
                .map(methodDeclaration -> new ReportRow(Arrays.asList(
                        methodDeclaration.declaringType().fullQualifiedName(),
                        methodDeclaration.methodSignature().asSimpleText()
                ))).collect(Collectors.toList());
    }

    public Report toReport() {
        return new Report() {
            @Override
            public Title title() {
                return new Title("文字列比較箇所");
            }

            @Override
            public ReportRow headerRow() {
                return new ReportRow(Arrays.asList("クラス名", "メソッド名"));
            }

            @Override
            public List<ReportRow> rows() {
                return StringComparingReport.this.rows();
            }
        };
    }
}
