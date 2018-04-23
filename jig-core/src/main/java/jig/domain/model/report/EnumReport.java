package jig.domain.model.report;

import jig.domain.model.angle.EnumAngle;
import jig.domain.model.characteristic.Characteristic;
import jig.domain.model.identifier.type.TypeIdentifierFormatter;
import jig.domain.model.japanese.JapaneseName;
import jig.domain.model.report.template.Report;
import jig.domain.model.report.template.ReportRow;
import jig.domain.model.report.template.Title;

import java.util.List;
import java.util.stream.Collectors;

public class EnumReport implements Report {

    private final List<EnumReport.Row> list;

    public EnumReport(List<EnumReport.Row> list) {
        this.list = list;
    }

    @Override
    public Title title() {
        return new Title("ENUM");
    }

    @Override
    public ReportRow headerRow() {
        return ReportRow.of(
                "クラス名",
                "クラス和名",
                "定数宣言",
                "フィールド",
                "使用箇所",
                "パラメーター有り",
                "振る舞い有り",
                "多態"
        );
    }

    @Override
    public List<ReportRow> rows() {
        return list.stream().map(row -> ReportRow.of(
                row.クラス名(),
                row.クラス和名(),
                row.定数宣言(),
                row.フィールド(),
                row.使用箇所(),
                row.パラメーター有り(),
                row.振る舞い有り(),
                row.多態()
        )).collect(Collectors.toList());
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

        public String 定数宣言() {
            return enumAngle.constantsDeclarations().toNameText();
        }

        public String フィールド() {
            return enumAngle.fieldDeclarations().toSignatureText();
        }

        public String 使用箇所() {
            return enumAngle.userTypeIdentifiers().asSimpleText();
        }

        public String パラメーター有り() {
            return enumAngle.characteristics().has(Characteristic.ENUM_PARAMETERIZED).toSymbolText();
        }

        public String 振る舞い有り() {
            return enumAngle.characteristics().has(Characteristic.ENUM_BEHAVIOUR).toSymbolText();
        }

        public String 多態() {
            return enumAngle.characteristics().has(Characteristic.ENUM_POLYMORPHISM).toSymbolText();
        }
    }
}
