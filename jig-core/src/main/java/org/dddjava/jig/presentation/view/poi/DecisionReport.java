package org.dddjava.jig.presentation.view.poi;

import org.dddjava.jig.domain.model.decisions.DecisionAngle;
import org.dddjava.jig.domain.model.decisions.DecisionAngles;
import org.dddjava.jig.domain.model.decisions.Layer;
import org.dddjava.jig.presentation.view.poi.report.ConvertibleItem;
import org.dddjava.jig.presentation.view.poi.report.Report;

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
