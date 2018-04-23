package jig.domain.model.report;

import jig.domain.model.declaration.annotation.ValidationAnnotationDeclaration;
import jig.domain.model.identifier.type.TypeIdentifierFormatter;
import jig.domain.model.japanese.JapaneseName;
import jig.domain.model.report.template.Report;
import jig.domain.model.report.template.ReportRow;
import jig.domain.model.report.template.Title;

import java.util.List;
import java.util.stream.Collectors;

public class ValidationReport implements Report {

    private final List<Row> list;

    public ValidationReport(List<Row> list) {
        this.list = list;
    }

    @Override
    public Title title() {
        return new Title("VALIDATION");
    }

    @Override
    public ReportRow headerRow() {
        return ReportRow.of(
                "クラス名",
                "クラス和名",
                "フィールドorメソッド",
                "アノテーション名",
                "アノテーション記述"
        );
    }

    @Override
    public List<ReportRow> rows() {
        return list.stream().map(row -> ReportRow.of(
                row.クラス名(),
                row.クラス和名(),
                row.フィールドorメソッド(),
                row.アノテーション名(),
                row.アノテーション記述()
        )).collect(Collectors.toList());
    }

    public static class Row {
        ValidationAnnotationDeclaration annotationDeclaration;
        JapaneseName japaneseName;
        TypeIdentifierFormatter identifierFormatter;

        public Row(ValidationAnnotationDeclaration annotationDeclaration, JapaneseName japaneseName, TypeIdentifierFormatter identifierFormatter) {
            this.annotationDeclaration = annotationDeclaration;
            this.japaneseName = japaneseName;
            this.identifierFormatter = identifierFormatter;
        }

        String クラス名() {
            return annotationDeclaration.declaringType().format(identifierFormatter);
        }

        String クラス和名() {
            return japaneseName.summarySentence();
        }

        String フィールドorメソッド() {
            return annotationDeclaration.annotateSimpleName();
        }

        String アノテーション名() {
            return annotationDeclaration.annotationType().asSimpleText();
        }

        String アノテーション記述() {
            return annotationDeclaration.annotationDescription().asText();
        }
    }
}
