package org.dddjava.jig.domain.model.data.members.instruction;

/**
 * 分岐数
 */
public class DecisionNumber {
    int value;

    public DecisionNumber(int value) {
        this.value = value;
    }

    public int intValue() {
        return value;
    }
}
