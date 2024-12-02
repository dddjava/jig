package org.dddjava.jig.infrastructure.view.html;

public interface TreeComponent extends Comparable<TreeComponent> {

    String name();

    String href();

    default boolean isPackage() {
        return this instanceof TreeComposite;
    }

    default boolean isDeprecated() {
        return false;
    }

    @Override
    default int compareTo(TreeComponent o) {
        // パッケージを優先
        if (isPackage() && !o.isPackage()) return -1;
        // fqnで比較する
        return href().compareTo(o.href());
    }
}
