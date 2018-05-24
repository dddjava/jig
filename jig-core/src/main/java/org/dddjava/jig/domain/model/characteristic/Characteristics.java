package org.dddjava.jig.domain.model.characteristic;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * 型の特徴の集合
 */
public class Characteristics {

    Set<Characteristic> characteristics;

    public Characteristics(Set<Characteristic> characteristics) {
        this.characteristics = characteristics;
    }

    public Satisfaction has(Characteristic characteristic) {
        return Satisfaction.of(characteristics.contains(characteristic));
    }

    public Layer toLayer() {
        for (Characteristic characteristic : characteristics) {
            if (characteristic == Characteristic.CONTROLLER) {
                return Layer.PRESENTATION;
            }
            if (characteristic == Characteristic.SERVICE) {
                return Layer.APPLICATION;
            }
            if (characteristic == Characteristic.REPOSITORY || characteristic == Characteristic.DATASOURCE) {
                return Layer.DATASOURCE;
            }
        }
        return Layer.OTHER;
    }

    public static Collector<Characteristics, ?, Characteristics> collector() {
        return Collectors.reducing(new Characteristics(Collections.emptySet()), (a, b) -> {
            HashSet<Characteristic> characteristics = new HashSet<>(a.characteristics);
            characteristics.addAll(b.characteristics);
            return new Characteristics(characteristics);
        });
    }
}
