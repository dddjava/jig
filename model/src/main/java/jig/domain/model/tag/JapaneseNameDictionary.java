package jig.domain.model.tag;

import jig.domain.model.thing.Name;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class JapaneseNameDictionary {

    final Map<Name, JapaneseName> map;

    public JapaneseNameDictionary() {
        this.map = new HashMap<>();
    }

    public boolean exists(Name name) {
        return map.containsKey(name);
    }

    public JapaneseName get(Name name) {
        return map.getOrDefault(name, new JapaneseName(""));
    }

    public void register(Name fqn, JapaneseName japaneseName) {
        map.put(fqn, japaneseName);
    }

    public void merge(JapaneseNameDictionary other) {
        map.putAll(other.map);
    }

    public String asText() {

        int maxLength = map.keySet().stream()
                .map(Name::value)
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
