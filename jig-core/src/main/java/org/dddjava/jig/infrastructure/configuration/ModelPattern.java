package org.dddjava.jig.infrastructure.configuration;

import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCode;

/**
 * モデルのパターン
 *
 * TODO これを使用している箇所は {@link org.dddjava.jig.domain.model.businessrules.BusinessRuleCondition} に移行していく。
 * 最終的にこのクラスは実行時引数を一時的に受けるためだけの物体になるか、なくしてしまう。
 */
public class ModelPattern {
    String pattern;

    public ModelPattern(String pattern) {
        this.pattern = pattern;
    }

    public ModelPattern() {
        this(".+\\.domain\\.model\\..+");
    }

    public boolean matches(TypeByteCode typeByteCode) {
        return typeByteCode.typeIdentifier().fullQualifiedName().matches(pattern);
    }
}
