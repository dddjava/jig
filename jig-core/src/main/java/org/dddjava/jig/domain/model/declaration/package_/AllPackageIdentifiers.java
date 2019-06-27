package org.dddjava.jig.domain.model.declaration.package_;

import java.util.ArrayList;
import java.util.List;

public class AllPackageIdentifiers {

    List<PackageIdentifier> list;

    AllPackageIdentifiers(List<PackageIdentifier> list) {
        this.list = new ArrayList<>();
        for (PackageIdentifier packageIdentifier : list) {
            if (this.list.contains(packageIdentifier)) {
                continue;
            }
            this.list.add(packageIdentifier);

            PackageIdentifier tmp = packageIdentifier.parent();
            while (tmp.hasName()) {
                if (this.list.contains(tmp)) break;
                this.list.add(tmp);
                tmp = tmp.parent();
            }
        }
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public List<PackageIdentifier> list() {
        return list;
    }
}
