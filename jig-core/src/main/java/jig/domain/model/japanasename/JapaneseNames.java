package jig.domain.model.japanasename;

import jig.domain.model.thing.Identifier;

import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

public class JapaneseNames {

    private final Map<Identifier, JapaneseName> map;

    public JapaneseNames(Map<Identifier, JapaneseName> map) {
        this.map = map;
    }

    public String asText() {
        int maxLength = map.keySet().stream()
                .map(Identifier::value)
                .mapToInt(String::length)
                .max()
                .orElseGet(() -> 0);

        return map.entrySet()
                .stream()
                .sorted(Comparator.comparing(o -> o.getKey().value()))
                .map(entry -> String.format("%-" + maxLength + "s :%s", entry.getKey().value(), entry.getValue().value()))
                .collect(Collectors.joining(System.lineSeparator()));
    }
}
