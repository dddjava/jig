package org.dddjava.jig.infrastructure.asm.ut;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@MyDeclarationAnnotation
public class MyClass<@MyTypeAnnotation X, Y> extends MySuperClass<Integer, X, Long> implements MyInterface<Y, @MyTypeAnnotation String>, MyInterface2<String, Y> {
}

class MySuperClass<T1, T2, T3> {
}

interface MyInterface<T1, T2> {
}

interface MyInterface2<T1, T2> {
}

@interface MyDeclarationAnnotation {
}

@Target(ElementType.TYPE_USE)
@interface MyTypeAnnotation {
}
