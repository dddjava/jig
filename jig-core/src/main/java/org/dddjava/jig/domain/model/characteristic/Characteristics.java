package org.dddjava.jig.domain.model.characteristic;

import java.util.Set;

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
}
