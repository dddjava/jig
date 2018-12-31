package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.type.Warning;

public class ClassFindFailException extends RuntimeException {
    public Warning warning() {
        return Warning.クラスが見つからないので中断する通知;
    }
}
