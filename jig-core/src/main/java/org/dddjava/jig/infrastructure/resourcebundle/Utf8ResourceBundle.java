package org.dddjava.jig.infrastructure.resourcebundle;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.PropertyResourceBundle;
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

    /**
     * ResourceBundleでUTF-8を読むためのControl実装。
     * Java9以降は不要だけどJava8をサポートしてるうちは必要になる。
     */
    static class Utf8Control extends ResourceBundle.Control {

        @Override
        public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload) throws IOException {
            try (InputStream is = loader.getResourceAsStream(toResourceName(toBundleName(baseName, locale), "properties"));
                 InputStreamReader inputStreamReader = new InputStreamReader(is, StandardCharsets.UTF_8);
                 Reader reader = new BufferedReader(inputStreamReader)) {
                return new PropertyResourceBundle(reader);
            }
        }
    }
}
