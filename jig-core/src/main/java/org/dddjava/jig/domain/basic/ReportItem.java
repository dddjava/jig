package org.dddjava.jig.domain.basic;

import org.dddjava.jig.domain.model.declaration.method.DecisionNumber;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.declaration.method.MethodNumber;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifiers;

public enum ReportItem {
    クラス名 {
        @Override
        public String convert(ReportContext obj) {
            return obj.formatTypeIdentifier(obj.value(TypeIdentifier.class));
        }
    },
    クラス和名 {
        @Override
        public String convert(ReportContext obj) {
            return obj.typeJapaneseName(obj.value(TypeIdentifier.class));
        }
    },
    メソッド名 {
        @Override
        public String convert(ReportContext obj) {
            return obj.value(MethodDeclaration.class).asSignatureSimpleText();
        }
    },
    メソッド数 {
        @Override
        public String convert(ReportContext obj) {
            return obj.value(MethodNumber.class).asText();
        }
    },
    メソッド一覧 {
        @Override
        public String convert(ReportContext obj) {
            return obj.value(MethodDeclarations.class).asSignatureAndReturnTypeSimpleText();
        }
    },
    分岐数 {
        @Override
        public String convert(ReportContext obj) {
            return obj.value(DecisionNumber.class).asText();
        }
    },
    使用箇所 {
        @Override
        public String convert(ReportContext obj) {
            return obj.value(TypeIdentifiers.class).asSimpleText();
        }
    },
    使用箇所数 {
        @Override
        public String convert(ReportContext obj) {
            return obj.value(UserNumber.class).asText();
        }
    };

    public abstract String convert(ReportContext obj);
}
