package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.type.Warning;

public class ClassFindFailException extends RuntimeException {
    public Warning warning() {
        return Warning.クラス検出異常;
    }
}
