package org.dddjava.jig.domain.model.validations;

import org.dddjava.jig.domain.model.declaration.annotation.ValidationAnnotationDeclaration;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifierFormatter;
import org.dddjava.jig.domain.model.japanese.JapaneseName;
import org.dddjava.jig.domain.basic.report.ConvertibleItem;
import org.dddjava.jig.domain.basic.report.Report;

import java.util.List;
import java.util.function.Function;

public class ValidationReport {

    private enum Items implements ConvertibleItem<Row> {
        クラス名(Row::クラス名),
        クラス和名(Row::クラス和名),
        フィールドorメソッド(Row::フィールドorメソッド),
        アノテーション名(Row::アノテーション名),
        アノテーション記述(Row::アノテーション記述);

        Function<Row, String> func;

        Items(Function<Row, String> func) {
            this.func = func;
        }

        @Override
        public String convert(Row row) {
            return func.apply(row);
        }
    }

    private final List<Row> list;

    public ValidationReport(List<Row> list) {
        this.list = list;
    }

    public Report<?> toReport() {
        return new Report<>("VALIDATION", list, Items.values());
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
