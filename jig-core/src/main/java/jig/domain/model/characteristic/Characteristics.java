package jig.domain.model.characteristic;

import java.util.EnumSet;

public class Characteristics {

    EnumSet<Characteristic> characteristics;

    public Characteristics(EnumSet<Characteristic> characteristics) {
        this.characteristics = characteristics;
    }

    public Satisfaction has(Characteristic characteristic) {
        return Satisfaction.of(characteristics.contains(characteristic));
    }
}
