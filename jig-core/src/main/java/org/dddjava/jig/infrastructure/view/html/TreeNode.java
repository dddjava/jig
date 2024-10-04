package org.dddjava.jig.infrastructure.view.html;

import org.dddjava.jig.domain.model.models.jigobject.class_.JigType;
import org.dddjava.jig.domain.model.parts.packages.PackageIdentifier;

import java.util.ArrayList;
import java.util.List;

public record TreeNode(
        String label,
        String id,
        List<TreeNode> children
) {

    public static TreeNode from(List<JigType> jigTypes) {
        var rootNode = new TreeNode("/", "(default)", new ArrayList<>());

        for (JigType jigType : jigTypes) {
            rootNode.addNode(jigType);
        }

        return null;
    }

    private void addNode(JigType jigType) {
        // 追加するノードを取ってくる
        var packageNode = getOrCreatePackageNode(jigType.packageIdentifier());

        var idText = jigType.identifier().htmlIdText();
        if (children.stream().noneMatch(child -> child.id.equals(idText))) {
            // なかったら追加する
            packageNode.children.add(new TreeNode(jigType.label(), idText, new ArrayList<>()));
        }
    }

    private TreeNode getOrCreatePackageNode(PackageIdentifier packageIdentifier) {
        var packageIdText = packageIdentifier.htmlIdText();
        if (id().equals(packageIdText)) {
            return this;
        } else {
            var parentNode = getOrCreatePackageNode(packageIdentifier.parent());
            var node = new TreeNode(
                    packageIdentifier.asText(),
                    packageIdText,
                    new ArrayList<>()
            );
            parentNode.children.add(node);
            return node;
        }
    }
}
