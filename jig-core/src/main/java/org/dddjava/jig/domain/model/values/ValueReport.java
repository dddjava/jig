package org.dddjava.jig.domain.model.values;

import org.dddjava.jig.domain.basic.report.ConvertibleItem;
import org.dddjava.jig.domain.basic.report.Report;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifierFormatter;
import org.dddjava.jig.domain.model.japanese.JapaneseName;

import java.util.List;
import java.util.function.Function;

/**
 * 値レポート
 */
public class ValueReport {

    /**
     * レポート項目
     */
    private enum Items implements ConvertibleItem<Row> {
        クラス名(Row::クラス名),
        クラス和名(Row::クラス和名),
        使用箇所数(Row::使用箇所数),
        使用箇所(Row::使用箇所);

        Function<Row, String> func;

        Items(Function<Row, String> func) {
            this.func = func;
        }

        @Override
        public String convert(Row row) {
            return func.apply(row);
        }
    }

    private final ValueKind valueKind;
    private final List<Row> list;

    public ValueReport(ValueKind valueKind, List<Row> list) {
        this.valueKind = valueKind;
        this.list = list;
    }

    public Report<?> toReport() {
        return new Report<>(valueKind.name(), list, Items.values());
    }

    public static class Row {
        ValueAngle valueAngle;
        JapaneseName japaneseName;
        TypeIdentifierFormatter identifierFormatter;

        public Row(ValueAngle valueAngle, JapaneseName japaneseName, TypeIdentifierFormatter identifierFormatter) {
            this.valueAngle = valueAngle;
            this.japaneseName = japaneseName;
            this.identifierFormatter = identifierFormatter;
        }

        String クラス名() {
            return valueAngle.typeIdentifier().format(identifierFormatter);
        }

        String クラス和名() {
            return japaneseName.summarySentence();
        }

        String 使用箇所() {
            return valueAngle.userTypeIdentifiers().asSimpleText();
        }

        String 使用箇所数() {
            return valueAngle.dependencyNumber().asText();
        }
    }
}
