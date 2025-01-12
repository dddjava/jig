package org.dddjava.jig.domain.model.documents.stationery;

import org.dddjava.jig.domain.model.data.packages.PackageComment;
import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

public class Labeler {
    JigDocumentContext jigDocumentContext;
    private Optional<String> commonPrefix = Optional.empty();

    public Labeler(JigDocumentContext jigDocumentContext) {
        this.jigDocumentContext = jigDocumentContext;
    }

    public String label(PackageIdentifier packageIdentifier, PackageIdentifier parent) {
        if (!packageIdentifier.asText().startsWith(parent.asText() + '.')) {
            // 引数の食い違いがあった場合に予期しない編集を行わないための回避コード。
            // TODO 通常は起こらないけれど起こらない実装にできてないので保険の実装。無くしたい。
            return label(packageIdentifier);
        }
        // parentでくくる場合にパッケージ名をの重複を省く
        String labelText = packageIdentifier.asText().substring(parent.asText().length());
        return addAliasIfExists(packageIdentifier, trimDot(labelText));
    }

    public String label(PackageIdentifier packageIdentifier) {
        String fqn = packageIdentifier.asText();
        String labelText = commonPrefix.map(String::length).map(index -> trimDot(fqn.substring(index)))
                .orElse(fqn);

        return addAliasIfExists(packageIdentifier, labelText);
    }

    private String addAliasIfExists(PackageIdentifier packageIdentifier, String labelText) {
        PackageComment packageComment = jigDocumentContext.packageComment(packageIdentifier);
        if (packageComment.exists()) {
            return packageComment.asText() + "\\n" + labelText;
        }
        return labelText;
    }

    public void applyContext(Collection<PackageIdentifier> groupingPackages, List<PackageIdentifier> allStandalonePackageIdentifiers) {
        // groupingPackagesとallStandalonePackageIdentifiersをまとめる
        Collection<PackageIdentifier> collection = new HashSet<>();
        collection.addAll(groupingPackages);
        collection.addAll(allStandalonePackageIdentifiers);

        applyContext(collection);
    }

    public void applyContext(Collection<PackageIdentifier> contextPackages) {
        // 引数が空ならreturn
        if (contextPackages.isEmpty()) {
            return;
        }

        // 全てで共通する部分を抜き出す
        String commonPrefix = null;
        for (PackageIdentifier currentPackageIdentifier : contextPackages) {
            PackageIdentifier currentParentPackageIdentifier = currentPackageIdentifier.parent();
            String currentText = currentParentPackageIdentifier.asText();
            if (commonPrefix == null) {
                commonPrefix = currentText;
                continue;
            }

            // sameRootTextとcurrentTextで前方から一致する部分の文字列を求める
            int commonPrefixLength = 0;
            for (int i = 0; i < Math.min(commonPrefix.length(), currentText.length()); i++) {
                if (commonPrefix.charAt(i) == currentText.charAt(i)) {
                    commonPrefixLength++;
                } else {
                    break;
                }
            }

            commonPrefix = commonPrefix.substring(0, commonPrefixLength);
        }
        this.commonPrefix = Optional.of(trimDot(commonPrefix));
    }

    public String contextDescription() {
        return "root: " + commonPrefix.orElse("(none)");
    }

    private String trimDot(String string) {
        return string.replaceAll("^\\.|\\.$", "");
    }
}
