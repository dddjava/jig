package org.dddjava.jig.infrastructure.asm.ut;

import java.math.BigDecimal;
import java.util.List;

public class MySutClass<T extends CharSequence> {
    byte primitiveField;

    int[] primitiveArrayField;

    String stringField;

    String[] stringArrayField;

    List<BigDecimal> genericField;

    List<BigDecimal[]> genericArrayField;

    List<T> genericTypeVariableField;

    List<T[]> genericTypeVariableArrayField;

    T typeVariableField;

    T[] typeVariableArrayField;

    T[][] typeVariable2DArrayField;

    void voidMethod(String myParameter) {
    }

    T typeVariableField(T typeVariableParameter) {
        return null;
    }

    <U extends Number> U typeParameterMethod(List<U> genericsParameter) {
        return null;
    }
}
