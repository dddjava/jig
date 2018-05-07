package org.dddjava.jig.presentation.view;

import org.dddjava.jig.domain.model.angle.EnumAngles;
import org.dddjava.jig.domain.model.angle.ServiceAngles;
import org.dddjava.jig.domain.model.identifier.namespace.PackageIdentifierFormatter;
import org.dddjava.jig.domain.model.japanese.JapaneseNameRepository;
import org.dddjava.jig.domain.model.relation.dependency.PackageDependencies;
import org.dddjava.jig.domain.model.report.Reports;
import org.dddjava.jig.presentation.view.graphvizj.EnumUsageToImageView;
import org.dddjava.jig.presentation.view.graphvizj.PackageDependencyToImageView;
import org.dddjava.jig.presentation.view.graphvizj.ServiceAngleToImageView;
import org.dddjava.jig.presentation.view.poi.ReportToExcelView;
import org.springframework.stereotype.Component;

@Component
public class JigViewResolver {

    private PackageIdentifierFormatter packageIdentifierFormatter;
    private JapaneseNameRepository japaneseNameRepository;

    public JigViewResolver(PackageIdentifierFormatter packageIdentifierFormatter, JapaneseNameRepository japaneseNameRepository) {
        this.packageIdentifierFormatter = packageIdentifierFormatter;
        this.japaneseNameRepository = japaneseNameRepository;
    }

    public LocalView dependencyWriter(PackageDependencies packageDependencies) {
        return new PackageDependencyToImageView(packageDependencies, packageIdentifierFormatter, japaneseNameRepository);
    }

    public LocalView serviceMethodCallHierarchy(ServiceAngles serviceAngles) {
        return new ServiceAngleToImageView(serviceAngles);
    }

    public LocalView classList(Reports reports) {
        return new ReportToExcelView(reports);
    }

    public LocalView enumUsage(EnumAngles enumAngles) {
        return new EnumUsageToImageView(enumAngles);
    }
}
