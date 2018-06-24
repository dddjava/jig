package org.dddjava.jig.presentation.view.handler;

import org.dddjava.jig.domain.model.declaration.namespace.PackageDepth;
import org.dddjava.jig.presentation.controller.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JigHandlerContext {

    ServiceMethodCallHierarchyController serviceMethodCallHierarchyController;
    ClassListController classListController;
    PackageDependencyController packageDependencyController;
    EnumUsageController enumUsageController;
    BooleanServiceTraceController booleanServiceTraceController;

    PackageDepth packageDepth;

    public JigHandlerContext(ServiceMethodCallHierarchyController serviceMethodCallHierarchyController,
                             ClassListController classListController,
                             PackageDependencyController packageDependencyController,
                             EnumUsageController enumUsageController,
                             BooleanServiceTraceController booleanServiceTraceController,
                             @Value("${depth:-1}") int packageDepth) {
        this.serviceMethodCallHierarchyController = serviceMethodCallHierarchyController;
        this.classListController = classListController;
        this.packageDependencyController = packageDependencyController;
        this.enumUsageController = enumUsageController;
        this.booleanServiceTraceController = booleanServiceTraceController;
        this.packageDepth = new PackageDepth(packageDepth);
    }

    public PackageDepth packageDepth() {
        return packageDepth;
    }
}
