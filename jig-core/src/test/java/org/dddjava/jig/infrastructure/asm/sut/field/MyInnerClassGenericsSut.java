package org.dddjava.jig.infrastructure.asm.sut.field;

public class MyInnerClassGenericsSut {

    Outer<String>.Inner<Integer> innerClassField;

    public static class Outer<T> {
        public class Inner<S> {
        }
    }
}
