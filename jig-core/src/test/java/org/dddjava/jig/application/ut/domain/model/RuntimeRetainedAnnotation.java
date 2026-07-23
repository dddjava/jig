package org.dddjava.jig.application.ut.domain.model;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * package-info に付与すると package-info.class の生成を強制するためのアノテーション。
 * 通常 package-info.java は package-info.class を生成しないが、
 * RUNTIME 保持のアノテーションを付けるとコンパイラが生成するようになる。
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface RuntimeRetainedAnnotation {
}
