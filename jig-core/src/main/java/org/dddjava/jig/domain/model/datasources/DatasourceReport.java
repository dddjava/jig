package org.dddjava.jig.domain.model.datasources;

import org.dddjava.jig.domain.basic.report.ConvertibleItem;
import org.dddjava.jig.domain.basic.report.Report;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifierFormatter;
import org.dddjava.jig.domain.model.japanese.JapaneseName;

import java.util.List;
import java.util.function.Function;

/**
 * データソースレポート
 */
public class DatasourceReport {

    /**
     * レポート項目
     */
    private enum Items implements ConvertibleItem<Row> {
        クラス名(Row::クラス名),
        メソッド名(Row::メソッド),
        メソッド戻り値の型(Row::メソッド戻り値の型),
        クラス和名(Row::クラス和名),
        INSERT(Row::insertTables),
        SELECT(Row::selectTables),
        UPDATE(Row::updateTables),
        DELETE(Row::deleteTables);

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

    public DatasourceReport(List<Row> list) {
        this.list = list;
    }

    public Report<?> toReport() {
        return new Report<>("REPOSITORY", list, Items.values());
    }

    public static class Row {
        DatasourceAngle datasourceAngle;
        JapaneseName japaneseName;
        TypeIdentifierFormatter identifierFormatter;

        public Row(DatasourceAngle datasourceAngle, JapaneseName japaneseName, TypeIdentifierFormatter identifierFormatter) {
            this.datasourceAngle = datasourceAngle;
            this.japaneseName = japaneseName;
            this.identifierFormatter = identifierFormatter;
        }

        String クラス名() {
            return datasourceAngle.method().declaringType().format(identifierFormatter);
        }

        String クラス和名() {
            return japaneseName.summarySentence();
        }

        String メソッド() {
            return datasourceAngle.method().asSignatureSimpleText();
        }

        String メソッド戻り値の型() {
            return datasourceAngle.returnType().asSimpleText();
        }

        String insertTables() {
            return datasourceAngle.insertTables().asText();
        }

        String selectTables() {
            return datasourceAngle.selectTables().asText();
        }

        String updateTables() {
            return datasourceAngle.updateTables().asText();
        }

        String deleteTables() {
            return datasourceAngle.deleteTables().asText();
        }
    }
}
