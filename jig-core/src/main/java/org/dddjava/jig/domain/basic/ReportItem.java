package org.dddjava.jig.domain.basic;

import org.dddjava.jig.domain.model.declaration.method.DecisionNumber;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.declaration.method.MethodNumber;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifiers;

/**
 * 一覧出力項目
 *
 * 出力項目の名前や取得方法、並び順の定義。
 *
 * TODO 配置場所や持ち方。列挙はともかく、処理は別クラスのはず
 */
public enum ReportItem {
    クラス名 {
        @Override
        public String convert(ReportContext obj) {
            return obj.formatTypeIdentifier(obj.value(TypeIdentifier.class));
        }
    },
    メソッド名 {
        @Override
        public String convert(ReportContext obj) {
            return obj.value(MethodDeclaration.class).asSignatureSimpleText();
        }
    },
    メソッド戻り値の型 {
        @Override
        public String convert(ReportContext obj) {
            return obj.value(MethodDeclaration.class).returnType().asSimpleText();
        }
    },

    イベントハンドラ {
        @Override
        public String convert(ReportContext obj) {
            return obj.value(Boolean.class) ? "◯" : "";
        }
    },

    クラス和名 {
        @Override
        public String convert(ReportContext obj) {
            return obj.typeJapaneseName(obj.value(TypeIdentifier.class));
        }
    },
    メソッド和名 {
        @Override
        public String convert(ReportContext obj) {
            return obj.methodJapaneseName(obj.value(MethodDeclaration.class).identifier());
        }
    },
    メソッド戻り値の型の和名 {
        @Override
        public String convert(ReportContext obj) {
            return obj.typeJapaneseName(obj.value(MethodDeclaration.class).returnType());
        }
    },
    メソッド引数の型の和名 {
        @Override
        public String convert(ReportContext obj) {
            return obj.value(MethodDeclaration.class).methodSignature().arguments().stream()
                    .map(obj::typeJapaneseName)
                    .collect(Text.collectionCollector());
        }
    },

    使用箇所数 {
        @Override
        public String convert(ReportContext obj) {
            return obj.value(UserNumber.class).asText();
        }
    },
    使用箇所 {
        @Override
        public String convert(ReportContext obj) {
            return obj.value(TypeIdentifiers.class).asSimpleText();
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

    汎用文字列 {
        @Override
        public String convert(ReportContext obj) {
            return obj.value(String.class);
        }
    },

    汎用真偽値 {
        @Override
        public String convert(ReportContext obj) {
            return obj.value(Boolean.class) ? "◯" : "";
        }
    };

    public abstract String convert(ReportContext obj);
}
