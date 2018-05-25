package org.dddjava.jig.presentation.view;

import org.dddjava.jig.domain.model.identifier.namespace.PackageDepth;
import org.dddjava.jig.domain.model.implementation.ProjectData;
import org.dddjava.jig.presentation.controller.EnumUsageController;
import org.dddjava.jig.presentation.controller.PackageDependencyController;
import org.dddjava.jig.presentation.controller.ServiceMethodCallHierarchyController;
import org.dddjava.jig.presentation.controller.classlist.ClassListController;
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

    // TODO
    ProjectData projectData = new ProjectData();

    public ProjectData getProjectData() {
        return projectData;
    }

    public ArgumentResolver argumentResolver() {
        return () -> projectData;
    }

    public void setProjectData(ProjectData projectData) {
        this.projectData = projectData;
    }
}
