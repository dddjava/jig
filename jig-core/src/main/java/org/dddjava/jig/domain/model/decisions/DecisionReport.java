package org.dddjava.jig.domain.model.decisions;

import org.dddjava.jig.domain.basic.report.ConvertibleItem;
import org.dddjava.jig.domain.basic.report.Report;

/**
 * 判断レポート
 */
public class DecisionReport {

    /**
     * レポート項目
     */
    enum Items implements ConvertibleItem<DecisionAngle> {
        クラス名 {
            @Override
            public String convert(DecisionAngle row) {
                return row.methodDeclaration().declaringType().fullQualifiedName();
            }
        },
        メソッド名 {
            @Override
            public String convert(DecisionAngle row) {
                return row.methodDeclaration().asSignatureSimpleText();
            }
        }
    }

    private final DecisionAngles decisionAngles;

    public DecisionReport(DecisionAngles decisionAngles) {
        this.decisionAngles = decisionAngles;
    }

    public Report<?> toReport(Layer layer) {
        return new Report<>(layer.asText(), decisionAngles.filter(layer), Items.values());
    }
}
