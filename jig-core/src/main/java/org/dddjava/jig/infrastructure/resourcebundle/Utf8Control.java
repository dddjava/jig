package org.dddjava.jig.infrastructure.resourcebundle;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * ResourceBundleでUTF-8を読むためのControl実装。
 * Java9以降は不要だけどJava8をサポートしてるうちは必要になる。
 */
public class Utf8Control extends ResourceBundle.Control {

    @Override
    public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload) throws IOException {
        try (InputStream is = loader.getResourceAsStream(toResourceName(toBundleName(baseName, locale), "properties"));
             InputStreamReader inputStreamReader = new InputStreamReader(is, StandardCharsets.UTF_8);
             Reader reader = new BufferedReader(inputStreamReader)) {
            return new PropertyResourceBundle(reader);
        }
    }
}
