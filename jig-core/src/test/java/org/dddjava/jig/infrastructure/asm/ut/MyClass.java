package org.dddjava.jig.infrastructure.asm.ut;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@MyAnnotation
public class MyClass<@MyTypeAnnotation X, Y> extends MySuperClass<X> implements MyInterface<Y, String> {
}

class MySuperClass<T> {
}

interface MyInterface<T1, T2> {
}

@interface MyAnnotation {
}

@Target(ElementType.TYPE_USE)
@interface MyTypeAnnotation {
}
