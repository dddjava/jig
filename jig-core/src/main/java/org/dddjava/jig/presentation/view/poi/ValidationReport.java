package org.dddjava.jig.presentation.view.poi;

import org.dddjava.jig.domain.model.declaration.annotation.ValidationAnnotatedMember;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifierFormatter;
import org.dddjava.jig.domain.model.japanese.JapaneseName;
import org.dddjava.jig.presentation.view.poi.report.ConvertibleItem;
import org.dddjava.jig.presentation.view.poi.report.Report;

import java.util.List;
import java.util.function.Function;

/**
 * バリデーションレポート
 */
public class ValidationReport {

    /**
     * レポート項目
     */
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
        ValidationAnnotatedMember annotatedMember;
        JapaneseName japaneseName;
        TypeIdentifierFormatter identifierFormatter;

        public Row(ValidationAnnotatedMember annotatedMember, JapaneseName japaneseName, TypeIdentifierFormatter identifierFormatter) {
            this.annotatedMember = annotatedMember;
            this.japaneseName = japaneseName;
            this.identifierFormatter = identifierFormatter;
        }

        String クラス名() {
            return annotatedMember.declaringType().format(identifierFormatter);
        }

        String クラス和名() {
            return japaneseName.summarySentence();
        }

        String フィールドorメソッド() {
            return annotatedMember.asSimpleNameText();
        }

        String アノテーション名() {
            return annotatedMember.annotationType().asSimpleText();
        }

        String アノテーション記述() {
            return annotatedMember.annotationDescription().asText();
        }
    }
}
