package org.dddjava.jig.presentation.view.handler;

import java.util.List;

public class Failures {

    private List<HandleResult> handleResultList;

    public Failures(List<HandleResult> handleResultList) {
        this.handleResultList = handleResultList;
    }

    @Override
    public String toString() {
        return handleResultList.toString();
    }
}
