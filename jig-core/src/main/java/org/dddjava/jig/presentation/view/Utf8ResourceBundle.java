package org.dddjava.jig.presentation.view;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * ResourceBundleでUTF-8を読むためのControl実装。
 * Java9以降は不要だけどJava8をサポートしてるうちは必要になる。
 */
public class Utf8ResourceBundle extends ResourceBundle.Control {

    static ResourceBundle getBundle() {
        // TODO user.language を使ってるけどpropertyにしたい
        Locale locale = Locale.getDefault();
        return ResourceBundle.getBundle("jig-document", locale, new Utf8ResourceBundle());
    }

    @Override
    public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload) throws IOException {
        try (InputStream is = loader.getResourceAsStream(toResourceName(toBundleName(baseName, locale), "properties"));
             InputStreamReader inputStreamReader = new InputStreamReader(is, StandardCharsets.UTF_8);
             Reader reader = new BufferedReader(inputStreamReader)) {
            return new PropertyResourceBundle(reader);
        }
    }
}
