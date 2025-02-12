package org.dddjava.jig.infrastructure.javaparser;

import org.dddjava.jig.domain.model.data.members.JavaMethodDeclarator;
import org.dddjava.jig.domain.model.data.term.Term;
import org.dddjava.jig.domain.model.data.term.TermIdentifier;
import org.dddjava.jig.domain.model.data.term.TermKind;

import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * 用語生成の共通処理
 *
 * Termを生成する過程をTermに持つと、TermがPackageIdentifierなどに依存してしまう。
 * Term自体は取り回し良くしておきたいので、Termには持たないことにした。
 * しかし生成過程はそれぞれに配置するには冗長なので、このクラスに分離しておく。
 */
public class TermFactory {

    public static Term fromPackage(TermIdentifier identifier, String javadocDescriptionText) {
        var text = normalize(javadocDescriptionText);
        var title = summaryText(text);
        return new Term(identifier, title, bodyText(title, text), TermKind.パッケージ);
    }

    public static Term fromClass(TermIdentifier identifier, String javadocDescriptionText) {
        var text = normalize(javadocDescriptionText);
        var title = summaryText(text);
        return new Term(identifier, title, bodyText(title, text), TermKind.クラス);
    }

    public static Term fromMethod(TermIdentifier identifier, JavaMethodDeclarator javaMethodDeclarator, String javadocDescriptionText) {
        var text = normalize(javadocDescriptionText);
        var title = summaryText(text);
        return new Term(identifier, title, bodyText(title, text), TermKind.メソッド, javaMethodDeclarator);
    }

    /**
     * インラインのlinkタグをテキストにするためのパターン
     */
    private static final Pattern INLINETAG_LINK_PATTERN = Pattern.compile("\\{@link\\s+(?:\\S+\\s+)?(\\S+)\\s*}");

    /**
     * 改行コードを統一するためのパターン。
     * Javaparserを使用する場合、ソースの改行コードに関わらずline.separatorに置き換えられる。JIGの出力は\nに寄せるので、ここで一律置き換える。
     */
    private static final Pattern LINE_SEPARATOR_PATTERN = Pattern.compile("\\R");

    static String normalize(String javadocDescriptionText) {
        return INLINETAG_LINK_PATTERN.matcher(
                LINE_SEPARATOR_PATTERN.matcher(javadocDescriptionText).replaceAll("\n")
        ).replaceAll("$1");
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
