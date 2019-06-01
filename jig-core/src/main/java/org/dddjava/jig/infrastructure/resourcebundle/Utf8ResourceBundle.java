package org.dddjava.jig.infrastructure.resourcebundle;

import java.util.Locale;
import java.util.ResourceBundle;

// FIXME infrastructure を domain, presentation から参照する形になっている
public class Utf8ResourceBundle {

    public static ResourceBundle documentBundle() {
        return new Utf8ResourceBundle().getBundle("jig-document");
    }

    public static ResourceBundle messageBundle() {
        return new Utf8ResourceBundle().getBundle("jig-messages");
    }

    ResourceBundle getBundle(String baseName) {
        // TODO user.language を使ってるけどpropertyにしたい
        Locale locale = Locale.getDefault();
        return ResourceBundle.getBundle(baseName, locale, new Utf8Control());
    }
}
