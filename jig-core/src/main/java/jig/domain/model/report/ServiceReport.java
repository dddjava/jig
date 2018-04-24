package jig.domain.model.report;

import jig.domain.model.angle.ServiceAngle;
import jig.domain.model.identifier.type.TypeIdentifierFormatter;
import jig.domain.model.japanese.JapaneseName;
import jig.domain.model.report.template.ItemRowConverter;
import jig.domain.model.report.template.Report;
import jig.domain.model.report.template.ReportImpl;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ServiceReport {

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

    private final List<Row> list;

    public ServiceReport(List<Row> list) {
        this.list = list;
    }

    public Report toReport() {
        List<ItemRowConverter<Row>> rowConverters =
                Arrays.stream(Items.values())
                        .map(item -> new ItemRowConverter<>(item, item.func))
                        .collect(Collectors.toList());
        return new ReportImpl<>("SERVICE", rowConverters, list);
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
