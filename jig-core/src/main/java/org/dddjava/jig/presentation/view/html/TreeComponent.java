package org.dddjava.jig.presentation.view.html;

public interface TreeComponent {

    String name();

    String href();

    default boolean isPackage() {
        return this instanceof TreeComposite;
    }

    default boolean isDeprecated() { return false; }
}
