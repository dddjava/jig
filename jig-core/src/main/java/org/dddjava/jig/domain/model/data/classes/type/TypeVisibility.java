package org.dddjava.jig.domain.model.data.classes.type;

/**
 * 型の可視性
 *
 * ソースコードではpublic,protected,privateが記述できますが、バイトコードではpublicか否かしかありません。
 *
 * https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-4.html#jvms-4.1-200-E.1
 */
public enum TypeVisibility {
    PUBLIC,
    NOT_PUBLIC
}
