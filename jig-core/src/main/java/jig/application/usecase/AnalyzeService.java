package jig.application.usecase;

import jig.application.service.DatasourceService;
import jig.application.service.DependencyService;
import jig.application.service.GlossaryService;
import jig.application.service.SpecificationService;
import jig.domain.model.project.SourceFactory;
import jig.domain.model.relation.dependency.PackageDependencies;
import org.springframework.stereotype.Service;

@Service
public class AnalyzeService {

    final SourceFactory sourceFactory;
    final SpecificationService specificationService;
    final DependencyService dependencyService;
    final GlossaryService glossaryService;
    final DatasourceService datasourceService;

    public AnalyzeService(SourceFactory sourceFactory,
                          SpecificationService specificationService,
                          DependencyService dependencyService,
                          GlossaryService glossaryService,
                          DatasourceService datasourceService) {
        this.sourceFactory = sourceFactory;
        this.specificationService = specificationService;
        this.dependencyService = dependencyService;
        this.glossaryService = glossaryService;
        this.datasourceService = datasourceService;
    }

    public PackageDependencies packageDependencies() {
        specificationService.importSpecification(sourceFactory.getSpecificationSources());
        glossaryService.importJapanese(sourceFactory.getPackageNameSources());

        return dependencyService.packageDependencies();
    }

    public void importProject() {
        specificationService.importSpecification(sourceFactory.getSpecificationSources());
        datasourceService.importDatabaseAccess(sourceFactory.getSqlSources());
        glossaryService.importJapanese(sourceFactory.getTypeNameSources());
    }
}
