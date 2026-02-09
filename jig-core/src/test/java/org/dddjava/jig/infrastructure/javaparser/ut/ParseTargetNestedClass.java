package org.dddjava.jig.infrastructure.javaparser.ut;

/**
 * 外側クラスコメント
 */
public class ParseTargetNestedClass {

    /**
     * 内側クラスコメント
     */
    static class Inner {
        /**
         * 内側メソッドコメント
         */
        void innerMethod() {
        }
    }

    /**
     * 内側enumコメント
     */
    enum InnerEnum {
        VALUE
    }

    /**
     * 内側recordコメント
     */
    record InnerRecord(String name) {
        /**
         * 内側recordメソッドコメント
         */
        String label() {
            return name;
        }
    }

    /**
     * 内側annotationコメント
     */
    @interface InnerAnnotation {
    }
}
