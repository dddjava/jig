package jig.domain.model.report;

import jig.domain.model.declaration.annotation.ValidationAnnotationDeclaration;
import jig.domain.model.identifier.type.TypeIdentifierFormatter;
import jig.domain.model.japanese.JapaneseName;
import jig.domain.model.report.template.Report;
import jig.domain.model.report.template.ReportRow;
import jig.domain.model.report.template.Title;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ValidationReport implements Report {

    private enum Items {
        クラス名(Row::クラス名),
        クラス和名(Row::クラス和名),
        フィールドorメソッド(Row::フィールドorメソッド),
        アノテーション名(Row::アノテーション名),
        アノテーション記述(Row::アノテーション記述);

        Function<Row, String> func;

        Items(Function<Row, String> func) {
            this.func = func;
        }
    }

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
        return ReportRow.of(Arrays.stream(Items.values()).map(Enum::name).toArray(String[]::new));
    }

    @Override
    public List<ReportRow> rows() {
        return list.stream()
                .map(row -> ReportRow.of(
                        Arrays.stream(Items.values())
                                .map(column -> column.func.apply(row))
                                .toArray(String[]::new)))
                .collect(Collectors.toList());
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
