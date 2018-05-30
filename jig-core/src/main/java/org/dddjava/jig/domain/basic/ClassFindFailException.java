package org.dddjava.jig.domain.basic;

public class ClassFindFailException extends RuntimeException {
    public Warning warning() {
        return Warning.クラス検出異常;
    }
}
