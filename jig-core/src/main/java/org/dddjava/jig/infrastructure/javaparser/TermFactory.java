package org.dddjava.jig.infrastructure.javaparser;

import org.dddjava.jig.domain.model.data.members.methods.JavaMethodDeclarator;
import org.dddjava.jig.domain.model.data.terms.Term;
import org.dddjava.jig.domain.model.data.terms.TermId;
import org.dddjava.jig.domain.model.data.terms.TermKind;

import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * 用語生成の共通処理
 *
 * Termを生成する過程をTermに持つと、TermがPackageIdentifierなどに依存してしまう。
 * Term自体は取り回し良くしておきたいので、Termには持たないことにした。
 * しかし生成過程はそれぞれに配置するには冗長なので、このクラスに分離しておく。
 */
class TermFactory {

    public static Term fromPackage(TermId identifier, String javadocDescriptionText) {
        var text = normalize(javadocDescriptionText);
        var title = summaryText(text);
        return new Term(identifier, title, bodyText(title, text), TermKind.パッケージ);
    }

    public static Term fromClass(TermId identifier, String javadocDescriptionText) {
        var text = normalize(javadocDescriptionText);
        var title = summaryText(text);
        return new Term(identifier, title, bodyText(title, text), TermKind.クラス);
    }

    public static Term fromMethod(TermId identifier, JavaMethodDeclarator javaMethodDeclarator, String javadocDescriptionText) {
        var text = normalize(javadocDescriptionText);
        var title = summaryText(text);
        return new Term(identifier, title, bodyText(title, text), TermKind.メソッド, javaMethodDeclarator);
    }

    public static Term fromField(TermId identifier, String javadocDescriptionText) {
        var text = normalize(javadocDescriptionText);
        var title = summaryText(text);
        return new Term(identifier, title, bodyText(title, text), TermKind.フィールド, javadocDescriptionText);
    }

    /**
     * インラインのlinkタグをテキストにするためのパターン
     */
    private static final Pattern INLINETAG_LINK_PATTERN = Pattern.compile("\\{@link\\s+(?:\\S+\\s+)?(\\S+)\\s*}");

    /**
     * インラインのcodeタグをテキストにするためのパターン
     */
    private static final Pattern INLINETAG_CODE_PATTERN = Pattern.compile("\\{@code\\s+(\\S+)\\s*}");

    /**
     * 改行コードを統一するためのパターン。
     * Javaparserを使用する場合、ソースの改行コードに関わらずline.separatorに置き換えられる。JIGの出力は\nに寄せるので、ここで一律置き換える。
     */
    private static final Pattern LINE_SEPARATOR_PATTERN = Pattern.compile("\\R");

    static String normalize(String javadocDescriptionText) {
        String 改行コード統一済み = LINE_SEPARATOR_PATTERN.matcher(javadocDescriptionText).replaceAll("\n");
        String linkタグ処理済み = INLINETAG_LINK_PATTERN.matcher(改行コード統一済み).replaceAll("$1");
        String codeタグ処理済み = INLINETAG_CODE_PATTERN.matcher(linkタグ処理済み).replaceAll("$1");
        return codeタグ処理済み;
    }

    static String summaryText(String value) {
        if (value.isEmpty()) {
            return "";
        }

        // 改行や句点の手前まで。
        return Stream.of(value.indexOf("\r\n"), value.indexOf("\n"), value.indexOf("。"))
                .filter(length -> length >= 0)
                .min(Integer::compareTo)
                .map(end -> value.substring(0, end))
                // 改行も句点も無い場合はそのまま返す
                .orElse(value);
    }

    static String bodyText(String title, String value) {
        // titleとvalueが同じなら本文なし
        // 改行や句点を除くために+1
        int beginIndex = title.length() + 1;
        if (value.length() <= beginIndex) {
            return "";
        }

        return value.substring(beginIndex).trim();
    }
}
