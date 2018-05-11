package org.dddjava.jig.domain.model.valueobjects;

import org.dddjava.jig.domain.model.characteristic.Characteristic;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifierFormatter;
import org.dddjava.jig.domain.model.japanese.JapaneseName;
import org.dddjava.jig.domain.basic.report.ConvertibleItem;
import org.dddjava.jig.domain.basic.report.Report;

import java.util.List;
import java.util.function.Function;

public class ValueObjectReport {

    private enum Items implements ConvertibleItem<Row> {
        クラス名(Row::クラス名),
        クラス和名(Row::クラス和名),
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

    private final Characteristic characteristic;
    private final List<Row> list;

    public ValueObjectReport(Characteristic characteristic, List<Row> list) {
        this.characteristic = characteristic;
        this.list = list;
    }

    public Report<?> toReport() {
        return new Report<>(characteristic.name(), list, Items.values());
    }

    public static class Row {
        ValueObjectAngle valueObjectAngle;
        JapaneseName japaneseName;
        TypeIdentifierFormatter identifierFormatter;

        public Row(ValueObjectAngle valueObjectAngle, JapaneseName japaneseName, TypeIdentifierFormatter identifierFormatter) {
            this.valueObjectAngle = valueObjectAngle;
            this.japaneseName = japaneseName;
            this.identifierFormatter = identifierFormatter;
        }

        String クラス名() {
            return valueObjectAngle.typeIdentifier().format(identifierFormatter);
        }

        String クラス和名() {
            return japaneseName.summarySentence();
        }

        String 使用箇所() {
            return valueObjectAngle.userTypeIdentifiers().asSimpleText();
        }
    }
}
