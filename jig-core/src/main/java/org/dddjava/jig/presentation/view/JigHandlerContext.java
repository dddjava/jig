package org.dddjava.jig.presentation.view;

import org.dddjava.jig.domain.model.declaration.namespace.PackageDepth;
import org.dddjava.jig.presentation.controller.ClassListController;
import org.dddjava.jig.presentation.controller.EnumUsageController;
import org.dddjava.jig.presentation.controller.PackageDependencyController;
import org.dddjava.jig.presentation.controller.ServiceMethodCallHierarchyController;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JigHandlerContext {

    ServiceMethodCallHierarchyController serviceMethodCallHierarchyController;
    ClassListController classListController;
    PackageDependencyController packageDependencyController;
    EnumUsageController enumUsageController;

    PackageDepth packageDepth;

    public JigHandlerContext(ServiceMethodCallHierarchyController serviceMethodCallHierarchyController,
                             ClassListController classListController,
                             PackageDependencyController packageDependencyController,
                             EnumUsageController enumUsageController,
                             @Value("${depth:-1}") int packageDepth) {
        this.serviceMethodCallHierarchyController = serviceMethodCallHierarchyController;
        this.classListController = classListController;
        this.packageDependencyController = packageDependencyController;
        this.enumUsageController = enumUsageController;
        this.packageDepth = new PackageDepth(packageDepth);
    }

    public PackageDepth packageDepth() {
        return packageDepth;
    }
}
