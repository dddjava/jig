package org.dddjava.jig.domain.model.report;

import org.dddjava.jig.domain.model.angle.DecisionAngle;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;

import java.util.List;

public class DecisionReport {

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

    private final DecisionAngle decisionAngle;

    public DecisionReport(DecisionAngle decisionAngle) {
        this.decisionAngle = decisionAngle;
    }

    public Report<?> toReport() {
        List<MethodDeclaration> list = decisionAngle.methods().list();
        return new Report<>("条件分岐箇所", list, Items.values());
    }
}
