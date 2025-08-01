package org.dddjava.jig.adapter.html;

import org.dddjava.jig.domain.model.data.packages.PackageId;
import org.dddjava.jig.domain.model.information.module.JigPackage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TreeComposite implements TreeComponent {

    private final JigPackage jigPackage;

    List<TreeComponent> list = new ArrayList<>();

    public TreeComposite(JigPackage jigPackage) {
        this.jigPackage = jigPackage;
    }

    @Override
    public String name() {
        return jigPackage.label();
    }

    @Override
    public String href() {
        return "#" + jigPackage.fqn();
    }

    public PackageId packageId() {
        return jigPackage.packageId();
    }

    public boolean hasChild() {
        return !list.isEmpty();
    }

    public List<TreeComponent> children() {
        return list.stream()
                .sorted()
                .toList();
    }

    public void addComponent(TreeComponent component) {
        list.add(component);
    }

    public TreeComposite resolveRootComposite() {
        TreeComposite root = this;
        while (root.list.size() == 1 && root.list.get(0) instanceof TreeComposite) {
            root = (TreeComposite) root.list.get(0);
        }
        return root;
    }

    public TreeComposite findComposite(PackageId packageId) {
        return findCompositeInternal(packageId)
                // 通常は見つかる
                // これが発生するのはこのインスタンスの子階層にないパッケージを引数にした場合
                .orElseThrow(() -> new IllegalStateException(packageId.asText() + " is not found in " + this.jigPackage.fqn()));
    }

    private Optional<TreeComposite> findCompositeInternal(PackageId packageId) {
        if (packageId.equals(this.jigPackage.packageId())) {
            return Optional.of(this);
        }
        for (TreeComponent child : list) {
            if (child instanceof TreeComposite) {
                Optional<TreeComposite> composite = ((TreeComposite) child).findCompositeInternal(packageId);
                if (composite.isPresent()) return composite;
            }
        }
        return Optional.empty();
    }
}
