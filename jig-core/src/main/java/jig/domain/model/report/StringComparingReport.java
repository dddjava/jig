package jig.domain.model.report;

import jig.domain.model.angle.DesignSmellAngle;
import jig.domain.model.declaration.method.MethodDeclaration;

import java.util.List;

public class StringComparingReport {

    enum Items implements ConvertibleItem<MethodDeclaration> {
        クラス名 {
            @Override
            public String convert(MethodDeclaration row) {
                return row.declaringType().fullQualifiedName();
            }
        },
        メソッド名 {
            @Override
            public String convert(MethodDeclaration row) {
                return row.methodSignature().asSimpleText();
            }
        };
    }

    private final DesignSmellAngle designSmellAngle;

    public StringComparingReport(DesignSmellAngle designSmellAngle) {
        this.designSmellAngle = designSmellAngle;
    }

    public Report toReport() {
        List<MethodDeclaration> list = designSmellAngle.stringComparingMethods().list();
        return new ConvertibleItemReport<>("文字列比較箇所", list, Items.values());
    }
}
