package org.dddjava.jig.presentation.view.html;

import org.dddjava.jig.domain.model.parts.alias.AliasFinder;
import org.dddjava.jig.domain.model.parts.package_.PackageIdentifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TreeComposite implements TreeComponent {

    private final PackageIdentifier packageIdentifier;
    private final AliasFinder aliasFinder;

    List<TreeComponent> list = new ArrayList<>();

    public TreeComposite(PackageIdentifier packageIdentifier, AliasFinder aliasFinder) {
        this.packageIdentifier = packageIdentifier;
        this.aliasFinder = aliasFinder;
    }

    @Override
    public String name() {
        return aliasFinder.find(packageIdentifier).summaryOrSimpleName();
    }

    @Override
    public String href() {
        return "#" + packageIdentifier.asText();
    }

    public PackageIdentifier packageIdentifier() {
        return packageIdentifier;
    }

    public boolean hasChild() {
        return !list.isEmpty();
    }

    public List<TreeComponent> children() {
        return list;
    }

    public List<TreeComponent> expandChildren() {
        if (list.size() == 1) {
            TreeComponent onlyOneChild = list.get(0);
            if (onlyOneChild instanceof TreeComposite) {
                TreeComposite composite = (TreeComposite) onlyOneChild;
                return composite.children();
            }
        }
        return list;
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
                .orElseThrow(() -> new IllegalStateException(packageIdentifier.asText() + " is not found in " + this.packageIdentifier.asText()));
    }

    private Optional<TreeComposite> findCompositeInternal(PackageIdentifier packageIdentifier) {
        if (packageIdentifier.equals(this.packageIdentifier)) {
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
