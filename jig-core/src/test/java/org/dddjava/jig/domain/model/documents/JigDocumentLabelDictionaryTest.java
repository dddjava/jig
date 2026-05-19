package org.dddjava.jig.domain.model.documents;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JigDocument の日本語ラベル（カノニカルキー）が jig-i18n.js の翻訳辞書にキーとして
 * 必ず存在することを担保するテスト。
 *
 * 言語切替時に、enum 側でラベル文字列を変更したのに JS 辞書の更新を忘れると、
 * 該当ドキュメントだけサイレントに翻訳されないリトロー級バグになる。
 */
class JigDocumentLabelDictionaryTest {

    @Test
    void JigDocument全件のラベルがjig_i18n_jsのen辞書にキーとして存在する() throws IOException {
        String i18nJs = readI18nJs();
        List<String> missing = Arrays.stream(JigDocument.values())
                .map(JigDocument::label)
                .filter(label -> !i18nJs.contains("\"" + label + "\":"))
                .toList();

        assertTrue(missing.isEmpty(),
                "jig-i18n.js の builtinDictionaries.en に翻訳キーが不足: " + missing);
    }

    private String readI18nJs() throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/templates/assets/jig-i18n.js")) {
            if (is == null) throw new IOException("jig-i18n.js not found in classpath");
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
