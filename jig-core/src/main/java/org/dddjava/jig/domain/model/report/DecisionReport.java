package org.dddjava.jig.domain.model.report;

import org.dddjava.jig.domain.model.angle.DecisionAngle;
import org.dddjava.jig.domain.model.angle.DecisionAngles;

public class DecisionReport {

    enum Items implements ConvertibleItem<DecisionAngle> {
        レイヤー {
            @Override
            public String convert(DecisionAngle row) {
                return row.typeLayer().asText();
            }
        },
        クラス名 {
            @Override
            public String convert(DecisionAngle row) {
                return row.declaringType().fullQualifiedName();
            }
        },
        メソッド名 {
            @Override
            public String convert(DecisionAngle row) {
                return row.methodSignature().asSimpleText();
            }
        };
    }

    private final DecisionAngles decisionAngles;

    public DecisionReport(DecisionAngles decisionAngles) {
        this.decisionAngles = decisionAngles;
    }

    public Report<?> toReport() {
        return new Report<>("条件分岐箇所", decisionAngles.listOnlyLayer(), Items.values());
    }
}
