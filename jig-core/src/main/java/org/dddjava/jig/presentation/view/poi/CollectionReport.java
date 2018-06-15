package org.dddjava.jig.presentation.view.poi;

import org.dddjava.jig.domain.model.collections.CollectionAngle;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifierFormatter;
import org.dddjava.jig.domain.model.japanese.JapaneseName;
import org.dddjava.jig.presentation.view.poi.report.ConvertibleItem;
import org.dddjava.jig.presentation.view.poi.report.Report;

import java.util.List;
import java.util.function.Function;

public class CollectionReport {

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

    private final List<Row> list;

    public CollectionReport(List<Row> list) {
        this.list = list;
    }

    public Report<?> toReport() {
        return new Report<>("COLLECTION", list, Items.values());
    }

    public static class Row {
        CollectionAngle collectionAngle;
        JapaneseName japaneseName;
        TypeIdentifierFormatter identifierFormatter;

        public Row(CollectionAngle collectionAngle, JapaneseName japaneseName, TypeIdentifierFormatter identifierFormatter) {
            this.collectionAngle = collectionAngle;
            this.japaneseName = japaneseName;
            this.identifierFormatter = identifierFormatter;
        }

        String クラス名() {
            return collectionAngle.typeIdentifier().format(identifierFormatter);
        }

        String クラス和名() {
            return japaneseName.summarySentence();
        }

        String 使用箇所() {
            return collectionAngle.userTypeIdentifiers().asSimpleText();
        }

        String 使用箇所数() {
            return collectionAngle.userNumber().asText();
        }
    }
}
