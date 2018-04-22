package jig.application.usecase;

import jig.application.service.DatasourceService;
import jig.application.service.GlossaryService;
import jig.application.service.SpecificationService;
import jig.domain.model.project.SourceFactory;
import org.springframework.stereotype.Service;

@Service
public class ImportLocalProjectService {

    final SourceFactory sourceFactory;
    final SpecificationService specificationService;
    final GlossaryService glossaryService;
    final DatasourceService datasourceService;

    public ImportLocalProjectService(SourceFactory sourceFactory,
                                     SpecificationService specificationService,
                                     GlossaryService glossaryService,
                                     DatasourceService datasourceService) {
        this.sourceFactory = sourceFactory;

        this.specificationService = specificationService;
        this.datasourceService = datasourceService;
        this.glossaryService = glossaryService;
    }

    public void importProject() {
        specificationService.importSpecification(sourceFactory.getSpecificationSources());

        datasourceService.importDatabaseAccess(sourceFactory.getSqlSources());

        glossaryService.importJapanese(sourceFactory.getTypeNameSources());
        glossaryService.importJapanese(sourceFactory.getPackageNameSources());
    }
}
