package jig.presentation.view;

import jig.domain.model.angle.ServiceAngles;
import jig.domain.model.identifier.namespace.PackageIdentifierFormatter;
import jig.domain.model.japanese.JapaneseNameRepository;
import jig.domain.model.relation.dependency.PackageDependencies;
import jig.domain.model.report.Reports;
import jig.presentation.view.graphvizj.PackageDependencyToImageView;
import jig.presentation.view.graphvizj.ServiceAngleToImageView;
import jig.presentation.view.poi.ReportToExcelView;
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
}
