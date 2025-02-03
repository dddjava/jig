package org.dddjava.jig.domain.model.data.types;

/**
 * 型の可視性
 *
 * ソースコードではpublic,protected,privateが記述できますが、バイトコードではpublicか否かしかありません。
 *
 * <a href="https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-4.html#jvms-4.1-200-E.1">JVMS/Chapter 4. The class File Format/4.1. The ClassFile Structure/Table 4.1-B. Class access and property modifiers</a>
 */
public enum JigTypeVisibility {
    PUBLIC,
    NOT_PUBLIC
}
