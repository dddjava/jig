package org.dddjava.jig.presentation.view;

import org.dddjava.jig.application.service.GlossaryService;
import org.dddjava.jig.domain.model.categories.EnumAngles;
import org.dddjava.jig.domain.model.identifier.namespace.PackageIdentifierFormatter;
import org.dddjava.jig.domain.model.japanese.JapaneseNameRepository;
import org.dddjava.jig.domain.model.networks.PackageDependencies;
import org.dddjava.jig.domain.basic.report.Reports;
import org.dddjava.jig.domain.model.services.ServiceAngles;
import org.dddjava.jig.presentation.view.graphvizj.EnumUsageToImageView;
import org.dddjava.jig.presentation.view.graphvizj.PackageDependencyToImageView;
import org.dddjava.jig.presentation.view.graphvizj.ServiceAngleToImageView;
import org.dddjava.jig.presentation.view.poi.ReportToExcelView;
import org.springframework.stereotype.Component;

@Component
public class ViewResolver {

    private PackageIdentifierFormatter packageIdentifierFormatter;
    private JapaneseNameRepository japaneseNameRepository;
    private GlossaryService glossaryService;

    public ViewResolver(PackageIdentifierFormatter packageIdentifierFormatter, JapaneseNameRepository japaneseNameRepository, GlossaryService glossaryService) {
        this.packageIdentifierFormatter = packageIdentifierFormatter;
        this.japaneseNameRepository = japaneseNameRepository;
        this.glossaryService = glossaryService;
    }

    public JigView<PackageDependencies> dependencyWriter() {
        return new PackageDependencyToImageView(packageIdentifierFormatter, japaneseNameRepository);
    }

    public JigView<ServiceAngles> serviceMethodCallHierarchy() {
        return new ServiceAngleToImageView(japaneseNameRepository);
    }

    public JigView<Reports> applicationList() {
        return new ReportToExcelView();
    }

    public JigView<EnumAngles> enumUsage() {
        return new EnumUsageToImageView(glossaryService);
    }

    public JigView<Reports> domainList() {
        return new ReportToExcelView();
    }
}
