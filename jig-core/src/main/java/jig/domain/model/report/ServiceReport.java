package jig.domain.model.report;

import jig.domain.model.angle.ServiceAngle;
import jig.domain.model.identifier.type.TypeIdentifierFormatter;
import jig.domain.model.japanese.JapaneseName;

import java.util.List;
import java.util.function.Function;

public class ServiceReport {

    private enum Items implements ConvertibleItem<Row> {
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

        @Override
        public String convert(Row row) {
            return func.apply(row);
        }
    }

    private final List<Row> list;

    public ServiceReport(List<Row> list) {
        this.list = list;
    }

    public Report toReport() {
        return new ConvertibleItemReport<>("SERVICE", list, Items.values());
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

        String メソッド() {
            return serviceAngle.method().asSimpleText();
        }

        String メソッド戻り値の型() {
            return serviceAngle.returnType().asSimpleText();
        }

        String イベントハンドラ() {
            return serviceAngle.usingFromController().toSymbolText();
        }

        String 使用しているフィールドの型() {
            return serviceAngle.usingFields().asSimpleText();
        }

        String 使用しているリポジトリのメソッド() {
            return serviceAngle.usingRepositoryMethods().asSimpleText();
        }
    }
}
