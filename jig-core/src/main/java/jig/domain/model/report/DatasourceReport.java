package jig.domain.model.report;

import jig.domain.model.angle.DatasourceAngle;
import jig.domain.model.characteristic.Characteristic;
import jig.domain.model.identifier.type.TypeIdentifierFormatter;
import jig.domain.model.japanese.JapaneseName;
import jig.domain.model.report.template.Report;
import jig.domain.model.report.template.ReportRow;
import jig.domain.model.report.template.Title;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DatasourceReport implements Report {

    private enum Items {
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
    }

    private final Characteristic characteristic;
    private final List<Row> list;

    public DatasourceReport(Characteristic characteristic, List<Row> list) {
        this.characteristic = characteristic;
        this.list = list;
    }

    @Override
    public Title title() {
        return new Title(characteristic.name());
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
