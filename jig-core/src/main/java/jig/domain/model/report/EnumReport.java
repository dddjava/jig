package jig.domain.model.report;

import jig.domain.model.angle.EnumAngle;
import jig.domain.model.characteristic.Characteristic;
import jig.domain.model.identifier.type.TypeIdentifierFormatter;
import jig.domain.model.japanese.JapaneseName;

import java.util.List;
import java.util.function.Function;

public class EnumReport {

    private enum Items implements ConvertibleItem<Row> {
        クラス名(Row::クラス名),
        クラス和名(Row::クラス和名),
        定数宣言(Row::定数宣言),
        フィールド(Row::フィールド),
        使用箇所(Row::使用箇所),
        パラメーター有り(Row::パラメーター有り),
        振る舞い有り(Row::振る舞い有り),
        多態(Row::多態);

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

    public EnumReport(List<Row> list) {
        this.list = list;
    }

    public Report toReport() {
        return new ConvertibleItemReport<>("ENUM", list, Items.values());
    }

    public static class Row {
        EnumAngle enumAngle;
        JapaneseName japaneseName;
        TypeIdentifierFormatter identifierFormatter;

        public Row(EnumAngle enumAngle, JapaneseName japaneseName, TypeIdentifierFormatter identifierFormatter) {
            this.enumAngle = enumAngle;
            this.japaneseName = japaneseName;
            this.identifierFormatter = identifierFormatter;
        }

        String クラス名() {
            return enumAngle.typeIdentifier().format(identifierFormatter);
        }

        String クラス和名() {
            return japaneseName.summarySentence();
        }

        String 定数宣言() {
            return enumAngle.constantsDeclarations().toNameText();
        }

        String フィールド() {
            return enumAngle.fieldDeclarations().toSignatureText();
        }

        String 使用箇所() {
            return enumAngle.userTypeIdentifiers().asSimpleText();
        }

        String パラメーター有り() {
            return enumAngle.characteristics().has(Characteristic.ENUM_PARAMETERIZED).toSymbolText();
        }

        String 振る舞い有り() {
            return enumAngle.characteristics().has(Characteristic.ENUM_BEHAVIOUR).toSymbolText();
        }

        String 多態() {
            return enumAngle.characteristics().has(Characteristic.ENUM_POLYMORPHISM).toSymbolText();
        }
    }
}
