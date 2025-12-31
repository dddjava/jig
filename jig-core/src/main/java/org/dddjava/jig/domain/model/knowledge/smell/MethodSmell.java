package org.dddjava.jig.domain.model.knowledge.smell;

import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.types.JigType;
import org.dddjava.jig.domain.model.knowledge.insight.MethodInsight;

import java.util.Optional;

/**
 * メソッドの不吉なにおい
 */
public record MethodSmell(JigMethod method,
                          JigType declaringJigType,
                          boolean notUseMember,
                          boolean primitiveInterface,
                          boolean referenceNull,
                          boolean nullDecision,
                          boolean returnsBoolean,
                          boolean returnsVoid
) {

    public static Optional<MethodSmell> from(JigMethod method, JigType declaringJigType) {
        // java.lang.Object由来は除外する
        if (method.isObjectMethod()) {
            return Optional.empty();
        }

        var methodInsight = new MethodInsight(method);

        // methodInsightからsmellとなりうるものを抽出
        var smellOfNotUseMember = methodInsight.smellOfNotUseMember();
        var smellOfPrimitiveInterface = methodInsight.smellOfPrimitiveInterface();
        var smellOfReferenceNull = methodInsight.smellOfReferenceNull();
        var smellOfNullDecision = methodInsight.smellOfNullDecision();
        var smellOfReturnsBoolean = methodInsight.smellOfReturnsBoolean();
        var smellOfReturnsVoid = methodInsight.smellOfReturnsVoid();

        if (!smellOfNotUseMember && !smellOfPrimitiveInterface && !smellOfReferenceNull && !smellOfNullDecision && !smellOfReturnsBoolean && !smellOfReturnsVoid) {
            return Optional.empty();
        }
        return Optional.of(new MethodSmell(method, declaringJigType,
                smellOfNotUseMember,
                smellOfPrimitiveInterface,
                smellOfReferenceNull,
                smellOfNullDecision,
                smellOfReturnsBoolean,
                smellOfReturnsVoid
        ));
    }

    public TypeId methodReturnType() {
        return method.returnType().id();
    }
}
