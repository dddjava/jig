package jig.domain.model.report;

import jig.domain.model.angle.DatasourceAngle;
import jig.domain.model.identifier.type.TypeIdentifierFormatter;
import jig.domain.model.japanese.JapaneseName;

import java.util.List;
import java.util.function.Function;

public class DatasourceReport {

    private enum Items implements ConvertibleItem<Row> {
        クラス名(Row::クラス名),
        クラス和名(Row::クラス和名),
        メソッド(Row::メソッド),
        メソッド戻り値の型(Row::メソッド戻り値の型),
        INSERT(Row::insertTables),
        SELECT(Row::selectTables),
        UPDATE(Row::updateTables),
        DELETE(Row::deleteTables);

        Function<Row, String> func;

        Items(Function<Row, String> func) {
            this.func = func;
        }

        @Override
        public RowConverter<Row> converter() {
            return new RowConverter<>(this, func);
        }
    }

    private final List<Row> list;

    public DatasourceReport(List<Row> list) {
        this.list = list;
    }

    public Report toReport() {
        return new ConvertibleItemReport<>("REPOSITORY", list, Items.values());
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

        public String メソッド() {
            return datasourceAngle.method().asSimpleText();
        }

        public String メソッド戻り値の型() {
            return datasourceAngle.returnType().asSimpleText();
        }

        public String insertTables() {
            return datasourceAngle.insertTables().asText();
        }

        public String selectTables() {
            return datasourceAngle.selectTables().asText();
        }

        public String updateTables() {
            return datasourceAngle.updateTables().asText();
        }

        public String deleteTables() {
            return datasourceAngle.deleteTables().asText();
        }
    }
}
