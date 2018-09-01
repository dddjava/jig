package org.dddjava.jig.domain.model.categories;

import java.util.List;

/**
 * 区分の特徴一覧
 */
public class CategoryCharacteristics {
    List<CategoryCharacteristic> list;

    public CategoryCharacteristics(List<CategoryCharacteristic> list) {
        this.list = list;
    }

    public boolean contains(CategoryCharacteristic characteristic) {
        return list.contains(characteristic);
    }
}
