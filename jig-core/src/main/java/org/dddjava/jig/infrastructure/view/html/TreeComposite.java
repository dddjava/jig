package org.dddjava.jig.infrastructure.view.html;

import org.dddjava.jig.domain.model.information.jigobject.package_.JigPackage;
import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;

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

    public PackageIdentifier packageIdentifier() {
        return jigPackage.packageIdentifier();
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

    public TreeComposite findComposite(PackageIdentifier packageIdentifier) {
        return findCompositeInternal(packageIdentifier)
                // 通常は見つかる
                // これが発生するのはこのインスタンスの子階層にないパッケージを引数にした場合
                .orElseThrow(() -> new IllegalStateException(packageIdentifier.asText() + " is not found in " + this.jigPackage.fqn()));
    }

    private Optional<TreeComposite> findCompositeInternal(PackageIdentifier packageIdentifier) {
        if (packageIdentifier.equals(this.jigPackage.packageIdentifier())) {
            return Optional.of(this);
        }
        for (TreeComponent child : list) {
            if (child instanceof TreeComposite) {
                Optional<TreeComposite> composite = ((TreeComposite) child).findCompositeInternal(packageIdentifier);
                if (composite.isPresent()) return composite;
            }
        }
        return Optional.empty();
    }
}
