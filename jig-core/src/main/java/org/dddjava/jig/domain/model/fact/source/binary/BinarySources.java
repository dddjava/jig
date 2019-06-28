package org.dddjava.jig.domain.model.fact.source.binary;

import java.util.List;

/**
 * バイナリソース一覧
 */
public class BinarySources {

    List<BinarySource> list;

    public BinarySources(List<BinarySource> list) {
        this.list = list;
    }

    public BinarySource toBinarySource() {
        return list.stream().reduce(new BinarySource(), BinarySource::merge);
    }

    public List<BinarySource> list() {
        return list;
    }

    public boolean nothing() {
        return list.isEmpty();
    }
}
