package org.dddjava.jig.presentation.view.html;

public interface TreeComponent {

    String name();

    String href();

    String descriptionText();

    default boolean isPackage() {
        return this instanceof TreeComposite;
    }
}
