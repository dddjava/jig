package org.dddjava.jig.presentation.view.html;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.alias.AliasFinder;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.package_.PackageIdentifier;

import java.util.ArrayList;
import java.util.List;

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

    @Override
    public String descriptionText() {
        return "";
    }

    public PackageIdentifier packageIdentifier() {
        return packageIdentifier;
    }

    public List<TreeComponent> children() {
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
}
