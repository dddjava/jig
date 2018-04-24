package jig.domain.model.report;

import jig.domain.model.angle.ServiceAngle;
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

public class ServiceReport implements Report {

    private enum Items {
        クラス名(Row::クラス名),
        クラス和名(Row::クラス和名),
        メソッド(Row::メソッド),
        メソッド戻り値の型(Row::メソッド戻り値の型),
        イベントハンドラ(Row::イベントハンドラ),
        使用しているフィールドの型(Row::使用しているフィールドの型),
        使用しているリポジトリのメソッド(Row::使用しているリポジトリのメソッド);

        Function<Row, String> func;

        Items(Function<Row, String> func) {
            this.func = func;
        }
    }

    private final Characteristic characteristic;
    private final List<Row> list;

    public ServiceReport(Characteristic characteristic, List<Row> list) {
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
        ServiceAngle serviceAngle;
        JapaneseName japaneseName;
        TypeIdentifierFormatter identifierFormatter;

        public Row(ServiceAngle serviceAngle, JapaneseName japaneseName, TypeIdentifierFormatter identifierFormatter) {
            this.serviceAngle = serviceAngle;
            this.japaneseName = japaneseName;
            this.identifierFormatter = identifierFormatter;
        }

        String クラス名() {
            return serviceAngle.method().declaringType().format(identifierFormatter);
        }

        String クラス和名() {
            return japaneseName.summarySentence();
        }

        public String メソッド() {
            return serviceAngle.method().asSimpleText();
        }

        public String メソッド戻り値の型() {
            return serviceAngle.returnType().asSimpleText();
        }

        public String イベントハンドラ() {
            return serviceAngle.usingFromController().toSymbolText();
        }

        public String 使用しているフィールドの型() {
            return serviceAngle.usingFields().asSimpleText();
        }

        public String 使用しているリポジトリのメソッド() {
            return serviceAngle.usingRepositoryMethods().asSimpleText();
        }
    }
}
