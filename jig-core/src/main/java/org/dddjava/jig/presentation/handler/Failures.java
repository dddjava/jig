package org.dddjava.jig.presentation.handler;

import java.util.List;

public class Failures {

    private final List<HandleResult> handleResultList;

    public Failures(List<HandleResult> handleResultList) {
        this.handleResultList = handleResultList;
    }

    @Override
    public String toString() {
        return handleResultList.toString();
    }
}
