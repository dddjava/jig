package org.dddjava.jig.presentation.handler;

import java.util.List;
import java.util.stream.Collectors;

public class HandleResults {
    List<HandleResult> list;

    HandleResults(List<HandleResult> list) {
        this.list = list;
    }

    public static HandleResults empty() {
        return new HandleResults(List.of());
    }

    public List<HandleResult> toList() {
        return list;
    }

    public String summaryText() {
        return list.toString();
    }

    public boolean completelySuccessful() {
        return list.stream().noneMatch(HandleResult::failure);
    }

    public Failures failures() {
        return new Failures(list.stream().filter(HandleResult::failure).collect(Collectors.toList()));
    }
}
