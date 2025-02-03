package org.dddjava.jig.infrastructure.asm.ut;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@MyDeclarationAnnotationForSource
@MyDeclarationAnnotationForClass
@MyDeclarationAnnotationForRuntime
public class MyClass<@MyTypeAnnotation X, @MyTypeParameterAnnotation Y>
        extends @MyTypeAnnotation MySuperClass<Integer, X, Long>
        implements MyInterface<Y, @MyTypeAnnotation String>, MyInterface2<String, Y> {
}

class MySuperClass<T1, T2, T3> {
}

interface MyInterface<T1, T2> {
}

interface MyInterface2<T1, T2> {
}

@Retention(RetentionPolicy.SOURCE)
@interface MyDeclarationAnnotationForSource {
}

@Retention(RetentionPolicy.CLASS)
@interface MyDeclarationAnnotationForClass {
}

@Retention(RetentionPolicy.RUNTIME)
@interface MyDeclarationAnnotationForRuntime {
}

@Target(ElementType.TYPE_USE)
@interface MyTypeAnnotation {
}

@Target(ElementType.TYPE_PARAMETER)
@interface MyTypeParameterAnnotation {
}
