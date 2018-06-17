package org.dddjava.jig.domain.basic;

import org.dddjava.jig.domain.model.declaration.method.DecisionNumber;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;

public enum ReportItem {
    クラス名 {
        @Override
        public String convert(ReportContext obj) {
            return ((TypeIdentifier) obj.value()).fullQualifiedName();
        }
    },
    メソッド名 {
        @Override
        public String convert(ReportContext obj) {
            return ((MethodDeclaration) obj.value()).asSignatureSimpleText();
        }
    },
    分岐数 {
        @Override
        public String convert(ReportContext obj) {
            return ((DecisionNumber) obj.value()).asText();
        }
    };

    public abstract String convert(ReportContext obj);
}
