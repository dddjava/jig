package org.dddjava.jig.domain.model.documents.stationery;

import org.dddjava.jig.domain.model.data.packages.PackageId;

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

    public String label(PackageId packageId, PackageId parent) {
        if (packageId.equals(parent)) {
            // parentと同じパッケージ
            return ".";
        }
        if (!packageId.asText().startsWith(parent.asText() + '.')) {
            // 引数の食い違いがあった場合に予期しない編集を行わないための回避コード。
            // TODO 通常は起こらないけれど起こらない実装にできてないので保険の実装。無くしたい。
            return label(packageId);
        }
        // parentでくくる場合にパッケージ名をの重複を省く
        String labelText = packageId.asText().substring(parent.asText().length());
        return addAliasIfExists(packageId, trimDot(labelText));
    }

    public String label(PackageId packageId) {
        String fqn = packageId.asText();
        String labelText = commonPrefix
                .filter(fqn::startsWith)
                .map(prefix -> trimDot(fqn.substring(prefix.length())))
                .orElse(fqn);

        return addAliasIfExists(packageId, labelText);
    }

    private String addAliasIfExists(PackageId packageId, String labelText) {
        var term = jigDocumentContext.packageTerm(packageId);
        if (term.title().equals(labelText)) {
            return labelText;
        }
        return term.title() + "\\n" + labelText;
    }

    public void applyContext(Collection<PackageId> groupingPackages, List<PackageId> allStandalonePackageIds) {
        // groupingPackagesとallStandalonePackageIdをまとめる
        Collection<PackageId> collection = new HashSet<>();
        collection.addAll(groupingPackages);
        collection.addAll(allStandalonePackageIds);

        applyContext(collection);
    }

    public void applyContext(Collection<PackageId> contextPackages) {
        // 引数が空ならreturn
        if (contextPackages.isEmpty()) {
            return;
        }

        // 全てで共通する部分を抜き出す
        String commonPrefix = null;
        for (PackageId currentPackageId : contextPackages) {
            Optional<PackageId> packageId = currentPackageId.parentIfExist();
            if (packageId.isEmpty()) {
                continue;
            }
            String currentText = packageId.orElseThrow().asText();

            packageId.map(PackageId::asText);
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
        this.commonPrefix = Optional.ofNullable(commonPrefix).map(this::trimDot);
    }

    public String contextDescription() {
        return "root: " + commonPrefix.orElse("(none)");
    }

    private String trimDot(String string) {
        return string.replaceAll("^\\.|\\.$", "");
    }
}
