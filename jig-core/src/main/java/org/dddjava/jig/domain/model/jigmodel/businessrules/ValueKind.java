package org.dddjava.jig.domain.model.jigmodel.businessrules;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifier;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 値の種類
 */
public enum ValueKind {
    IDENTIFIER(BusinessRuleCategory.文字列) {
        @Override
        public boolean matches(FieldDeclarations fieldDeclarations) {
            return fieldDeclarations.matches(new TypeIdentifier(String.class));
        }
    },
    NUMBER(BusinessRuleCategory.数値) {
        @Override
        public boolean matches(FieldDeclarations fieldDeclarations) {
            return fieldDeclarations.matches(new TypeIdentifier(BigDecimal.class))
                    || fieldDeclarations.matches(new TypeIdentifier(Long.class))
                    || fieldDeclarations.matches(new TypeIdentifier(Integer.class))
                    || fieldDeclarations.matches(new TypeIdentifier(long.class))
                    || fieldDeclarations.matches(new TypeIdentifier(int.class));
        }
    },
    DATE(BusinessRuleCategory.日付) {
        @Override
        public boolean matches(FieldDeclarations fieldDeclarations) {
            return fieldDeclarations.matches(new TypeIdentifier(LocalDate.class));
        }
    },
    TERM(BusinessRuleCategory.期間) {
        @Override
        public boolean matches(FieldDeclarations fieldDeclarations) {
            return fieldDeclarations.matches(new TypeIdentifier(LocalDate.class), new TypeIdentifier(LocalDate.class));
        }
    };

    BusinessRuleCategory businessRuleCategory;

    ValueKind(BusinessRuleCategory businessRuleCategory) {
        this.businessRuleCategory = businessRuleCategory;
    }

    abstract boolean matches(FieldDeclarations fieldDeclarations);
}
